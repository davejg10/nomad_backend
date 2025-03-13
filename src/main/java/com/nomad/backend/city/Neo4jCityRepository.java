package com.nomad.backend.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.repositories.Neo4jCommonCityRepository;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.*;

@Log4j2
@Configuration
public class Neo4jCityRepository extends Neo4jCommonCityRepository {

    
    public Neo4jCityRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jMappingContext schema) {
        super(neo4jClient, objectMapper, schema);
    }

    // This method returns a city and fetches all of its routes to cities that have a particular countryId
    // If the origin city has no valid routes, then just the city is returned.
    public Optional<Neo4jCity> findByIdFetchRoutesByCountryId(String id, String routesCountryId) {
        Optional<Neo4jCity> city = neo4jClient
                .query("""
                    MATCH (city:City {id: $id})
                    MATCH (city) -[toCountry:OF_COUNTRY]-> (country:Country)
                    OPTIONAL MATCH (city) -[route:ROUTE]-> (t)
                    MATCH (t) -[toTargetCountry:OF_COUNTRY]-> (targetCountry:Country {id: $routesCountryId})
                    RETURN city, toCountry, country, collect(route) as routes, collect(t) as targetCity, targetCountry
                """)
                .bind(id).to("id")
                .bind(routesCountryId).to("routesCountryId")
                .fetchAs(Neo4jCity.class)
                .mappedBy((typeSystem, record) -> {
                    Neo4jCity fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Neo4jCountry fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());
                    Neo4jCountry targetCitiesCountry = countryMapper.apply(typeSystem, record.get("targetCountry").asNode());

                    Set<Neo4jRoute> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty()) {
                        Map<String, Neo4jCity> targetCitiesMap = mapTargetCities(typeSystem, record.get("targetCity"), targetCitiesCountry);
                        routes = mapRoutes(typeSystem, record.get("routes"), targetCitiesMap);
                    }

                    return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
                })
                .first();
        if (city.isPresent()) return city;
        return findById(id);
    }
}
