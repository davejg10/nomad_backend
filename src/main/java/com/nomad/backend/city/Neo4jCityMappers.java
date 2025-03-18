package com.nomad.backend.city;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Component;

import com.nomad.data_library.domain.TransportType;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.exceptions.Neo4jMissingKeyException;
import com.nomad.data_library.repositories.Neo4jCommonCityMappers;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

@Component
public class Neo4jCityMappers extends Neo4jCommonCityMappers {

    public Neo4jCityMappers(Neo4jMappingContext schema) {
        super(schema);
    }

    /*
    Returns a city with all ROUTE, OF_COUNTRY, relationships mapped. Note that each of the targetCities has the same country (but this can be different to sourceCities country)
     */
    protected BiFunction<TypeSystem, Record, Neo4jCity> cityNoMetricsSameCountry() {
        return (typeSystem, record) -> {
            if (!record.containsKey("city") || !record.containsKey("country") || !record.containsKey("routes") || !record.containsKey("targetCities") ||
                    !record.containsKey("targetCityCountry") )
            {
                throw new Neo4jMissingKeyException("cityNoMetricsSameCountry");
            }

            Neo4jCity fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
            Neo4jCountry fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());
            Neo4jCountry targetCitiesCountry = countryMapper.apply(typeSystem, record.get("targetCityCountry"));

            Set<Neo4jRoute> routes = Set.of();
            if (!record.get("routes").asList().isEmpty()) {
                routes = mapRoutes(typeSystem, record.get("routes"), record.get("targetCities"), targetCitiesCountry);
            }

            return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getShortDescription(), fetchedCity.getPrimaryBlobUrl(), fetchedCity.getCoordinate(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
        };
    }

    private Set<Neo4jRoute> mapRoutes(TypeSystem typeSystem, Value routes, Value targetCities, Neo4jCountry citiesCountry) {
        AtomicInteger index = new AtomicInteger(0);
        return new HashSet<>(routes.asList(route -> new Neo4jRoute(
                    route.get("id").asString(),
                    cityMapper.apply(typeSystem, targetCities.get(index.getAndIncrement()).asNode()).withCountry(citiesCountry),
                    route.get("popularity").asDouble(),
                    route.get("time").asDouble(),
                    route.get("cost").asDouble(),
                    TransportType.valueOf(route.get("transportType").asString())
        )));
    }
    
}
