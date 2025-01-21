package com.nomad.backend.cities;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CityRepository extends Neo4jRepository<City, String> {

    Optional<City> findByName(String cityName);

    @Query("""
            MATCH (city:City {name: $cityName})
            OPTIONAL MATCH (city:City {name: $cityName}) -[rel:ROUTE]-> (targetCity:City)
            RETURN city, collect(rel), collect(targetCity)
           """)
    Optional<City> findByNameReturnRoutes(String cityName);

    @Query("MATCH (city:City) WHERE city.name IN $cityNames RETURN city")
    Set<City> findByNameIn(Set<String> cityNames);

    @Query("""
        MERGE (c:City {name: $city.name})
        ON CREATE SET c.description = $city.description,
            c.countryName = $city.countryName,
            c.id = randomUUID()
        
        WITH c
        UNWIND $routes AS routeData
        
        MERGE (t:City {name: routeData.targetCityName})
        ON CREATE SET t.description = routeData.targetCityDescription,
            t.countryName = routeData.targetCityCountryName,
            c.id = randomUUID()
        
        WITH c, t, routeData   
        OPTIONAL MATCH (c)-[r:ROUTE {
            transportType: routeData.transportType
        }]->(t)
        DELETE r
        
        CREATE (c)-[rel:ROUTE {
            popularity: routeData.popularity,
            weight: routeData.weight,
            transportType: routeData.transportType,
            id: randomUUID()
        }]->(t)
        
        RETURN c, collect(rel), collect(t)
    """)
    City saveCityDepth0(Map<String, String> city, List<Map<String, String>> routes);

}
