package com.nomad.backend.cities;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CityRepository extends Neo4jRepository<City, String> {

    @Query("MATCH (city:City {name: $cityName}) RETURN city")
    Optional<City> findByName(String cityName);

    @Query("MATCH (city:City {name: $cityName}) " +
            "OPTIONAL MATCH (city:City {name: $cityName}) -[rel:ROUTE]-> (targetCity:City) " +
            "RETURN city, collect(rel), collect(targetCity)")
    Optional<City> findByNameReturnRoutes(String cityName);

    @Query("MATCH (city:City) WHERE city.name IN $cityNames RETURN city")
    Set<City> findByNameIn(Set<String> cityNames);

}
