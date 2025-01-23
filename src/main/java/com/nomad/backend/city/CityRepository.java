package com.nomad.backend.city;

import com.nomad.backend.city.domain.City;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CityRepository extends Neo4jRepository<City, String> {

    @Query("""
        MERGE (c:City {name: $mapifiedCity.name})
        ON CREATE SET c.id = randomUUID()
        SET c.description = $mapifiedCity.description,
            c.cityMetrics = $mapifiedCity.cityMetrics
        
        WITH c
        MATCH(country:Country {name: $mapifiedCity.country.name})
        MERGE (country)-[fromCountry:HAS_CITY]->(c)
        ON CREATE SET fromCountry.id = randomUUID()
        MERGE (c)-[toCountry:OF_COUNTRY]->(country)
        ON CREATE SET toCountry.id = randomUUID()
      
        WITH c
        UNWIND $mapifiedCity.routes AS routeData
        
        MERGE (t:City {name: routeData.targetCity.name})
        ON CREATE SET t.description = routeData.targetCity.description,
            t.id = randomUUID(),
        
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
    void saveCityWithDepth0(Map<String, Object> mapifiedCity);


    @Query("""
       MATCH (city:City {id: $id})
       OPTIONAL MATCH (city) -[rel:OF_COUNTRY]-> (country:Country)
       RETURN city, collect(rel), collect(country)
    """)
    Optional<City> findById(String id);

    @Query("""
        MATCH (city:City)
        OPTIONAL MATCH (city) -[toCountry:OF_COUNTRY]-> (country:Country)
        OPTIONAL MATCH (city) -[toCity:ROUTE]-> (targetCity:City)
        RETURN city, collect(toCountry), collect(country), collect(toCity), collect(targetCity)
    """)
    List<City> findAll();



    @Query("MATCH (city:City) WHERE city.name IN $cityNames RETURN city")
    Set<City> findByNameIn(Set<String> cityNames);

    @Query("""
        MATCH (city:City {id: $id})
        OPTIONAL MATCH (city) -[toCountry:OF_COUNTRY]-> (country:Country)
        OPTIONAL MATCH (city) -[toCity:ROUTE]-> (targetCity:City)
        RETURN city, collect(toCountry), collect(country), collect(toCity), collect(targetCity)
    """)
    Optional<City> findByIdFetchRoutes(String id);


}
