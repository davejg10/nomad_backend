package com.nomad.backend.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.repositories.Neo4jCommonCityRepository;

import lombok.extern.log4j.Log4j2;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.*;
import java.util.function.BiFunction;

@Log4j2
@Configuration
public class Neo4jCityRepository extends Neo4jCommonCityRepository {

    private BiFunction<TypeSystem, Record, Neo4jCity> cityNoMetricsSameCountry;
    
    public Neo4jCityRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCityMappers neo4jCityMappers) {
        super(neo4jClient, objectMapper, neo4jCityMappers);
        this.cityNoMetricsSameCountry = neo4jCityMappers.cityNoMetricsSameCountry();
    }

    // This method returns a city and fetches all of its routes to cities that have a particular countryId
    // If the origin city has no valid routes, then just the city is returned.
    public Optional<Neo4jCity> findByIdFetchRoutesByCountryId(String id, String routesCountryId) {
        Optional<Neo4jCity> city = neo4jClient
                .query("""
                    MATCH (city:City {id: $id})
                    MATCH (city) -[:OF_COUNTRY]-> (country)

                    OPTIONAL MATCH (city) -[route:ROUTE]-> (t)
                    
                    MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country {id: $routesCountryId})

                    RETURN city, country, collect(route) as routes, collect(t) as targetCities, targetCityCountry
                """)
                .bind(id).to("id")
                .bind(routesCountryId).to("routesCountryId")
                .fetchAs(Neo4jCity.class)
                .mappedBy(cityNoMetricsSameCountry)
                .first();
        if (city.isPresent()) return city;
        return findById(id);
    }
}
