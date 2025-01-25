package com.nomad.backend.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.domain.Country;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.*;
import java.util.function.BiFunction;

@Slf4j
@Configuration
public class CityRepository {

    private final Neo4jClient client;
    private final ObjectMapper objectMapper;
    private final BiFunction<TypeSystem, MapAccessor, City> cityMapper;
    private final BiFunction<TypeSystem, MapAccessor, Country> countryMapper;

    public CityRepository(Neo4jClient client, ObjectMapper objectMapper, Neo4jMappingContext schema) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.cityMapper = schema.getRequiredMappingFunctionFor(City.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Country.class);
    }

    private Set<Route> mapRoutes(TypeSystem typeSystem, Value routesValue, Value targetCitiesValue) {
        Map<String, City> targetCityMap = new HashMap<>();

        targetCitiesValue.asList(targetCity -> {
            String nodeElementId = targetCity.asNode().elementId();
            City createdTargetCity = cityMapper.apply(typeSystem, targetCity.asNode());
            targetCityMap.put(nodeElementId, createdTargetCity);
            return null;
        });

        Set<Route> routes = new HashSet<>(routesValue
                .asList(route -> {
                    String endNodeElementId = route.asRelationship().endNodeElementId().toString();
                    String elementId = route.asEntity().elementId().toString();
                    return new Route(
                            elementId,
                            targetCityMap.get(endNodeElementId),
                            route.get("popularity").asInt(),
                            route.get("weight").asInt(),
                            TransportType.valueOf(route.get("transportType").asString())
                    );
                }));

        return routes;
    }

    public Optional<City> findById(String id) {
        return findById(id, false);
    }

    public Optional<City> findByIdFetchRoutes(String id) {
        return findById(id, true);
    }

    private Optional<City> findById(String id, boolean includeRoutes) {
        Optional<City> city = client
                .query("""
                    MATCH (city:City {id: $id})
                    OPTIONAL MATCH (city) -[toCountry:OF_COUNTRY]-> (country:Country)
                    OPTIONAL MATCH (city) -[route:ROUTE]-> (t)
                    RETURN city, toCountry, country, collect(route) as routes, collect(t) as targetCity
                """)
                .bind(id).to("id")
                .fetchAs(City.class)
                .mappedBy((typeSystem, record) -> {
                    City fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Country fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Route> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty() && includeRoutes) {
                        routes = mapRoutes(typeSystem, record.get("routes"), record.get("targetCity"));
                    }

                    return new City(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getDescription(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
                })
                .first();
        return city;
    }

    public Set<City> findAllCities() {
        Collection<City> allCities = client
                .query("""
                    MATCH (city:City)
                    OPTIONAL MATCH (city)-[ofCountry:OF_COUNTRY]->(country)
                    OPTIONAL MATCH (city)-[route:ROUTE]->(t)
                    RETURN city, ofCountry, country, collect(route) as routes, collect(t) as targetCity
                """)
                .fetchAs(City.class)
                .mappedBy((typeSystem, record) -> {
                    City fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Country fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Route> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty()) {
                        routes = mapRoutes(typeSystem, record.get("routes"), record.get("targetCity"));
                    }
                    return new City(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getDescription(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
                })
                .all();
        return new HashSet<>(allCities);
    }

    public Optional<City> findByName(String name) {
        Optional<City> city = client
                .query("""
                    MATCH (city:City {name: $name})
                    OPTIONAL MATCH (city)-[ofCountry:OF_COUNTRY]->(country)
                    OPTIONAL MATCH (city)-[route:ROUTE]->(t)
                    RETURN city, ofCountry, country, collect(route) as routes, collect(t) as targetCity
                """)
                .bind(name).to("name")
                .fetchAs(City.class)
                .mappedBy((typeSystem, record) -> {
                    City fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Country fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Route> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty()) {
                        routes = mapRoutes(typeSystem, record.get("routes"), record.get("targetCity"));
                    }
                    return new City(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getDescription(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
                })
                .first();
        return city;
    }

    public City saveCityWithDepth0(City city) {

        Map<String, Object> cityAsMap = mapifyCity(city);

        client.query("""
            MERGE (c:City {name: $name})
            ON CREATE SET c.id = randomUUID()
            SET c.description = $description,
                c.cityMetrics = $cityMetrics
            
            WITH c
            MATCH(country:Country {name: $country.name})
            MERGE (country)-[fromCountry:HAS_CITY]->(c)
            ON CREATE SET fromCountry.id = randomUUID()
            MERGE (c)-[toCountry:OF_COUNTRY]->(country)
            ON CREATE SET toCountry.id = randomUUID()
          
            WITH c
            UNWIND $routes AS routeData
            
            MERGE (t:City {name: routeData.targetCity.name})
            ON CREATE SET t.description = routeData.targetCity.description,
                t.id = randomUUID(),
                t.cityMetrics = routeData.targetCity.cityMetrics
            
            WITH c, t, routeData
            MATCH(country:Country {name: routeData.targetCity.country.name})
            MERGE (country)-[fromCountry:HAS_CITY]->(t)
            ON CREATE SET fromCountry.id = randomUUID()
            MERGE (t)-[toCountry:OF_COUNTRY]->(country)
            ON CREATE SET toCountry.id = randomUUID()
            
            WITH c, t, routeData   
            OPTIONAL MATCH (c)-[r:ROUTE {
                   transportType: routeData.transportType
            }]->(t)
            WHERE r.popularity <> routeData.popularity OR r.weight <> routeData.weight
            DELETE r
            
            MERGE (c)-[rel:ROUTE {
                popularity: routeData.popularity,
                weight: routeData.weight,
                transportType: routeData.transportType
            }]->(t)
            ON CREATE SET rel.id = randomUUID()
        """)
        .bindAll(cityAsMap)
        .run();

        return findByName(cityAsMap.get("name").toString()).get();
    }

    public Map<String, Object> mapifyCity(City city) {
        Map<String, Object> cityAsMap = objectMapper.convertValue(city, Map.class);
        return cityAsMap;
    }
}
