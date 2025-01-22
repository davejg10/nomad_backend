package com.nomad.backend.cities;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CityRepository extends Neo4jRepository<City, String> {


    @Query("MATCH (city:City {name: $cityName}) RETURN city")
    Optional<City> findByName(String cityName);

    @Query("""
            MATCH (city:City {name: $cityName})
            OPTIONAL MATCH (city) -[rel:ROUTE]-> (targetCity:City)
            RETURN city, collect(rel), collect(targetCity)
           """)
    Optional<City> findByNameReturnRoutes(String cityName);

    @Query("MATCH (city:City) WHERE city.name IN $cityNames RETURN city")
    Set<City> findByNameIn(Set<String> cityNames);

    @Query("""
        MERGE (c:City {name: $mapifiedCity.name})
        ON CREATE SET c.id = randomUUID()
        SET c.description = $mapifiedCity.description,
            c.countryName = $mapifiedCity.countryName,
            c.sailingMetric = $mapifiedCity.cityMetrics.sailing.metric,
            c.foodMetric = $mapifiedCity.cityMetrics.food.metric,
            c.nightlifeMetric = $mapifiedCity.cityMetrics.nightlife.metric
        
        WITH c
        UNWIND $mapifiedCity.routes AS routeData
        
        MERGE (t:City {name: routeData.targetCity.name})
        ON CREATE SET t.description = routeData.targetCity.description,
            t.countryName = routeData.targetCity.countryName,
            t.id = randomUUID()
        
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
    City saveCityWithDepth0(Map<String, Object> mapifiedCity);

}
