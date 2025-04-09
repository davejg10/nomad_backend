package com.nomad.backend.city.neo4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.domain.RouteInfoDTO;
import com.nomad.common_utils.domain.TransportType;
import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.CityMetric;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.repositories.Neo4jCommonCityRepository;

import lombok.extern.log4j.Log4j2;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Log4j2
@Configuration
public class Neo4jCityRepository extends Neo4jCommonCityRepository {

    private final Neo4jCityMappers neo4jCityMappers;

    public Neo4jCityRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCityMappers neo4jCityMappers) {
        super(neo4jClient, objectMapper, neo4jCityMappers);
        this.neo4jCityMappers = neo4jCityMappers;
    }

    // This method returns a city and fetches all of its routes to cities that have a particular countryId
    // If the origin city has no valid routes, then just the city is returned.
    public Optional<Neo4jCity> findByIdFetchRoutesByTargetCityCountryId(String id, String targetCityCountryId) {
        Optional<Neo4jCity> city = neo4jClient
                .query("""
                    MATCH (city:City {id: $id})
                    OPTIONAL MATCH (city)-[:OF_COUNTRY]->(country)

                    WITH city, country
                    OPTIONAL MATCH (city)-[:HAS_METRIC]->(m:Metric)
                    WITH city, country, collect(m) as cityMetrics
                
                    OPTIONAL MATCH (city)-[route:ROUTE]->(t)
                
                    MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country {id: $targetCityCountryId})
                
                    WITH city, country, cityMetrics, route, t, targetCityCountryRel, targetCityCountry
                    OPTIONAL MATCH (t)-[targetCityMetricRel:HAS_METRIC]->(targetCityMetric:Metric)
                
                    WITH city, country, cityMetrics, route, t, targetCityCountryRel, targetCityCountry,
                        collect(targetCityMetricRel) as targetCityMetricRels,
                        collect(targetCityMetric) as targetCityMetrics
                        
                    RETURN city, country, cityMetrics,
                        collect(route) as routes,
                        collect(t) as targetCities,
                        collect(targetCityCountryRel) as targetCityCountryRels,
                        collect(targetCityCountry) as targetCityCountries,
                        collect(targetCityMetricRels) as targetCityMetricRels,
                        collect(targetCityMetrics) as targetCityMetrics
                """)
                .bind(id).to("id")
                .bind(targetCityCountryId).to("targetCityCountryId")
                .fetchAs(Neo4jCity.class)
                .mappedBy(neo4jCityMappers.cityWithAllRelationshipsMapper())
                .first();
        if (city.isPresent()) return city;
        return findById(id);
    }

    public Set<RouteInfoDTO> fetchRoutesByTargetCityCountryIdOrderByPreferences(String id, String targetCityCountryId, Map<String, String> cityCriteriaPreferences, int costPreference) {
        List<RouteInfoDTO> results = neo4jClient
                .query("""
                    MATCH (city:City {id: $id})
            
                    WITH city
            
                    MATCH (city)-[route:ROUTE]->(targetCity)
            
                    WITH city, route, targetCity
                    MATCH (targetCity) -[:OF_COUNTRY]-> (targetCityCountry:Country {id: $targetCityCountryId})
            
                    WITH route, targetCity, targetCityCountry, round(point.distance(city.coordinate, targetCity.coordinate)) / 1000 as distance
                    MATCH (targetCity)-[:HAS_METRIC]->(targetCityMetric:Metric)
            
                    WITH route, targetCity, targetCityCountry, distance, collect(targetCityMetric) as targetCityMetrics
            
                    WITH route, targetCity, targetCityCountry, distance, targetCityMetrics,
                    REDUCE(sum = 0, m IN targetCityMetrics | sum +
                            CASE m.criteria
                                WHEN 'FOOD' THEN m.metric * ($foodPreference/3)
                                WHEN 'NIGHTLIFE' THEN m.metric * ($nightlifePreference/3)
                                WHEN 'SAILING' THEN m.metric * ($sailingPreference/3)
                                ELSE 0
                    END) AS attributeScore
            
                    WITH route, targetCity, targetCityCountry, distance, targetCityMetrics, attributeScore,
                    CASE $costPreference
                    WHEN 3 THEN 0  // No cost penalty
                    WHEN 2 THEN (toFloat(route.averageCost)/50.0)  // Moderate penalty, normalized
                    WHEN 1 THEN (toFloat(route.averageCost)/25.0)  // Full penalty, normalized
                    ELSE (toFloat(route.averageCost)/25.0)
                    END AS costPenalty
            
                    WITH route, targetCity, targetCityCountry, distance, targetCityMetrics, (attributeScore - costPenalty) AS totalScore, attributeScore, costPenalty
            
                    WITH route, targetCity, targetCityCountry, distance, targetCityMetrics, totalScore, attributeScore, costPenalty
                    ORDER BY totalScore DESC
      
                    RETURN route, targetCity, targetCityCountry, distance, targetCityMetrics, totalScore, attributeScore, costPenalty
                """)
                .bind(id).to("id")
                .bind(targetCityCountryId).to("targetCityCountryId")
                .bind(Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.FOOD.name()))).to("foodPreference")
                .bind(Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.NIGHTLIFE.name()))).to("nightlifePreference")
                .bind(Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.SAILING.name()))).to("sailingPreference")
                .bind(costPreference).to("costPreference")
                .fetchAs(RouteInfoDTO.class)
                .mappedBy((TypeSystem typeSystem, Record record) -> {


                    Neo4jCountry targetCityCountry = neo4jCityMappers.countryMapper.apply(typeSystem, record.get("targetCityCountry"));
                    Neo4jCity targetCity = neo4jCityMappers.cityMapper.apply(typeSystem, record.get("targetCity"));
                    Set<CityMetric> targetCityMetrics = new HashSet<>(record.get("targetCityMetrics").asList(targetCityMetric -> new CityMetric(
                            CityCriteria.valueOf(targetCityMetric.get("criteria").asString()),
                            targetCityMetric.get("metric").asDouble()
                    )));

                    targetCity = targetCity.withCountry(targetCityCountry).withCityMetrics(targetCityMetrics);

                    Value route = record.get("route");
                    Neo4jRoute theRoute = new Neo4jRoute(
                            route.get("id").asString(),
                            targetCity,
                            route.get("popularity").asDouble(),
                            Duration.parse(route.get("averageDuration").asString()),
                            new BigDecimal(route.get("averageCost").asString()),
                            TransportType.valueOf(route.get("transportType").asString())
                    );
                    log.info("TargetCity: {}, TransportType: {}, score: {}, attributeScore: {}, costPenalty: {}", targetCity.getName(), theRoute.getTransportType(), record.get("totalScore"), record.get("attributeScore"), record.get("costPenalty"));

                    return new RouteInfoDTO(theRoute, record.get("totalScore").asDouble(), record.get("distance").asDouble());
                    // return theRoute;

                })
                .all().stream().toList();

        return new LinkedHashSet<>(results);
    }
}
