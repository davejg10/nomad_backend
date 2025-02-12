package com.nomad.backend.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.domain.Country;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Log4j2
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

    // TODO think there will be ways of making this more efficient. Note that there are ids within node sets start = 5 for instace..
    private Map<String, City> mapTargetCitiesAndCountries(TypeSystem typeSystem, Value targetCitiesValue, Value targetCityCountryRelValue, Value targetCityCountryValue) {
        Map<String, Country> targetCountriesMap = new HashMap<>();
        Map<String, City> targetCitiesMap = new HashMap<>();

        targetCityCountryValue.asList(targetCountry -> {
            String nodeElementId = targetCountry.asNode().elementId();
            Country createdTargetCountry = countryMapper.apply(typeSystem, targetCountry.asNode());
            targetCountriesMap.put(nodeElementId, createdTargetCountry);
            return null;
        });

        targetCitiesValue.asList(targetCity -> {
            String targetCityNodeElementId = targetCity.asNode().elementId();
            String targetCountryNodeElementId = targetCityCountryRelValue
                    .asList(targetCityCountry -> targetCityCountry.asRelationship())
                    .stream()
                    .filter(targetCityCountryRel -> targetCityCountryRel.startNodeElementId().equals(targetCityNodeElementId))
                    .findFirst().get().endNodeElementId();

            Country targetCityCountry = targetCountriesMap.get(targetCountryNodeElementId);
            City createdTargetCity = cityMapper.apply(typeSystem, targetCity.asNode());
            City createdTargetCityWithCountry = new City(createdTargetCity.getId(), createdTargetCity.getName(), createdTargetCity.getDescription(), createdTargetCity.getCityMetrics(), createdTargetCity.getRoutes(), targetCityCountry);
            targetCitiesMap.put(targetCityNodeElementId, createdTargetCityWithCountry);
            return null;
        });

        return targetCitiesMap;
    }

    private Map<String, City> mapTargetCities(TypeSystem typeSystem, Value targetCitiesValue, Country country) {
        Map<String, City> targetCitiesMap = new HashMap<>();

        targetCitiesValue.asList(targetCity -> {
            String targetCityNodeElementId = targetCity.asNode().elementId();
            City createdTargetCity = cityMapper.apply(typeSystem, targetCity.asNode());
            City createdTargetCityWithCountry = new City(createdTargetCity.getId(), createdTargetCity.getName(), createdTargetCity.getDescription(), createdTargetCity.getCityMetrics(), createdTargetCity.getRoutes(), country);
            targetCitiesMap.put(targetCityNodeElementId, createdTargetCityWithCountry);
            return null;
        });

        return targetCitiesMap;
    }

    private Set<Route> mapRoutes(TypeSystem typeSystem, Value routesValue, Map<String, City> targetCitiesMap) {

        Set<Route> routes = new HashSet<>(routesValue
                .asList(route -> {
                    String endNodeElementId = route.asRelationship().endNodeElementId().toString();
                    String elementId = route.asEntity().elementId().toString();
                    return new Route(
                            elementId,
                            targetCitiesMap.get(endNodeElementId),
                            route.get("popularity").asDouble(),
                            route.get("time").asDouble(),
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

    public Optional<City> findById(String id, boolean includeRoutes) {
        Optional<City> city = client
                .query("""
                    MATCH (city:City {id: $id})
                    OPTIONAL MATCH (city) -[toCountry:OF_COUNTRY]-> (country:Country)
                    OPTIONAL MATCH (city) -[route:ROUTE]-> (t)
                    OPTIONAL MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country)
                    RETURN city, toCountry, country, collect(route) as routes, collect(t) as targetCity, collect(targetCityCountryRel) as targetCityCountryRel, collect(targetCityCountry) as targetCityCountry
                """)
                .bind(id).to("id")
                .fetchAs(City.class)
                .mappedBy((typeSystem, record) -> {
                    City fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Country fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Route> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty() && includeRoutes) {
                        Map<String, City> targetCitiesMap = mapTargetCitiesAndCountries(typeSystem, record.get("targetCity"), record.get("targetCityCountryRel"), record.get("targetCityCountry"));
                        routes = mapRoutes(typeSystem, record.get("routes"), targetCitiesMap);
                    }

                    return new City(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getDescription(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
                })
                .first();
        return city;
    }

    // This method returns a city and fetches all of its routes to cities that have a particular countryId
    // If the origin city has no valid routes, then just the city is returned.
    public Optional<City> findByIdFetchRoutesByCountryId(String id, String routesCountryId) {
        Optional<City> city = client
                .query("""
                    MATCH (city:City {id: $id})
                    MATCH (city) -[toCountry:OF_COUNTRY]-> (country:Country)
                    OPTIONAL MATCH (city) -[route:ROUTE]-> (t)
                    MATCH (t) -[toTargetCountry:OF_COUNTRY]-> (targetCountry:Country {id: $routesCountryId})
                    RETURN city, toCountry, country, collect(route) as routes, collect(t) as targetCity, targetCountry
                """)
                .bind(id).to("id")
                .bind(routesCountryId).to("routesCountryId")
                .fetchAs(City.class)
                .mappedBy((typeSystem, record) -> {
                    City fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Country fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());
                    Country targetCitiesCountry = countryMapper.apply(typeSystem, record.get("targetCountry").asNode());

                    Set<Route> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty()) {
                        Map<String, City> targetCitiesMap = mapTargetCities(typeSystem, record.get("targetCity"), targetCitiesCountry);
                        routes = mapRoutes(typeSystem, record.get("routes"), targetCitiesMap);
                    }

                    return new City(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getDescription(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
                })
                .first();
        if (city.isPresent()) return city;
        return findById(id);
    }

    public Set<City> findAllCities() {
        Collection<City> allCities = client
                .query("""
                    MATCH (city:City)
                    OPTIONAL MATCH (city)-[ofCountry:OF_COUNTRY]->(country)
                    OPTIONAL MATCH (city)-[route:ROUTE]->(t)
                    OPTIONAL MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country)
                    RETURN city, ofCountry, country, collect(route) as routes, collect(t) as targetCity, collect(targetCityCountryRel) as targetCityCountryRel, collect(targetCityCountry) as targetCityCountry
                """)
                .fetchAs(City.class)
                .mappedBy((typeSystem, record) -> {
                    City fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Country fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Route> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty()) {
                        Map<String, City> targetCitiesMap = mapTargetCitiesAndCountries(typeSystem, record.get("targetCity"), record.get("targetCityCountryRel"), record.get("targetCityCountry"));
                        routes = mapRoutes(typeSystem, record.get("routes"), targetCitiesMap);
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
                    OPTIONAL MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country)
                    RETURN city, ofCountry, country, collect(route) as routes, collect(t) as targetCity, collect(targetCityCountryRel) as targetCityCountryRel, collect(targetCityCountry) as targetCityCountry
                """)
                .bind(name).to("name")
                .fetchAs(City.class)
                .mappedBy((typeSystem, record) -> {
                    City fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Country fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Route> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty()) {
                        Map<String, City> targetCitiesMap = mapTargetCitiesAndCountries(typeSystem, record.get("targetCity"), record.get("targetCityCountryRel"), record.get("targetCityCountry"));
                        routes = mapRoutes(typeSystem, record.get("routes"), targetCitiesMap);
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
            WHERE r.popularity <> routeData.popularity OR r.time <> routeData.time
            DELETE r
            
            MERGE (c)-[rel:ROUTE {
                popularity: routeData.popularity,
                time: routeData.time,
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
