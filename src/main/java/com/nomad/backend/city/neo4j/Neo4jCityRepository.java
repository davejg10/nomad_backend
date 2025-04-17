package com.nomad.backend.city.neo4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.domain.CityInfoDTO;
import com.nomad.backend.domain.RouteInfoDTO;
import com.nomad.common_utils.domain.TransportType;
import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.CityMetric;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.Neo4jCommonCityRepository;

import lombok.extern.log4j.Log4j2;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;


import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    public Set<CityInfoDTO> fetchCitiesByCountryIdsOrderByPreferences(Set<String> selectedCountryIds, Map<String, String> cityCriteriaPreferences, int costPreference) {
        List<CityInfoDTO> results = neo4jClient
                .query("""
                    MATCH (city:City)
                    MATCH (city) -[:OF_COUNTRY]-> (country:Country)
                    WHERE country.id IN $selectedCountryIds
            
                    WITH city, country
                    MATCH (city)-[:HAS_METRIC]->(cityMetric:Metric)
            
                    WITH city,  country, collect(cityMetric) as cityMetrics
            
                    WITH city, country, cityMetrics,
                    REDUCE(sum = 0, m IN cityMetrics | sum +
                            CASE m.criteria
                                WHEN 'FOOD' THEN m.metric * ($foodPreference/3)
                                WHEN 'NIGHTLIFE' THEN m.metric * ($nightlifePreference/3)
                                WHEN 'SAILING' THEN m.metric * ($sailingPreference/3)
                                ELSE 0
                    END) AS attributeScore
            
                    WITH city, country, cityMetrics, attributeScore as totalScore
                        
                    WITH city, country, cityMetrics, totalScore
                    ORDER BY totalScore DESC
      
                    RETURN city, country, cityMetrics, totalScore
                """)
                .bind(selectedCountryIds).to("selectedCountryIds")
                .bind(Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.FOOD.name()))).to("foodPreference")
                .bind(Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.NIGHTLIFE.name()))).to("nightlifePreference")
                .bind(Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.SAILING.name()))).to("sailingPreference")
                .bind(costPreference).to("costPreference")
                .fetchAs(CityInfoDTO.class)
                .mappedBy((TypeSystem typeSystem, Record record) -> {

                    Neo4jCountry country = neo4jCityMappers.countryMapper.apply(typeSystem, record.get("country"));
                    Neo4jCity city = neo4jCityMappers.cityMapper.apply(typeSystem, record.get("city"));
                    Set<CityMetric> cityMetrics = new HashSet<>(record.get("cityMetrics").asList(cityMetric -> new CityMetric(
                            CityCriteria.valueOf(cityMetric.get("criteria").asString()),
                            cityMetric.get("metric").asDouble()
                    )));

                    city = city.withCountry(country).withCityMetrics(cityMetrics);

                    return new CityInfoDTO(city, record.get("totalScore").asDouble());
                })
                .all().stream().toList();

        return new LinkedHashSet<>(results);
    }

    public Set<RouteInfoDTO> fetchRoutesByCityIdAndCountryIdsOrderByPreferences(String id, Set<String> selectedCountryIds, Map<String, String> cityCriteriaPreferences, int costPreference) {
        List<RouteInfoDTO> results = neo4jClient
                .query("""
                    MATCH (city:City {id: $id})
            
                    WITH city
            
                    MATCH (city)-[route:ROUTE]->(targetCity)
            
                    WITH city, route, targetCity
                    MATCH (targetCity) -[:OF_COUNTRY]-> (targetCityCountry:Country)
                    WHERE targetCityCountry.id IN $selectedCountryIds
            
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
                .bind(selectedCountryIds).to("selectedCountryIds")
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
                })
                .all().stream().toList();

        return new LinkedHashSet<>(results);
    }

    public Iterator<Record> beamSearch(Map<String, Object> params) throws Neo4jGenericException {

        try {
            Iterator<Record> results = neo4jClient.query("""
                UNWIND $currentPaths AS currentPath
                // 2. Get the last node ID and its associated scores from the previous path
                WITH currentPath,
                    last(currentPath.nodeIds) AS lastNodeId

                // 3. Match the last node object (needed for coordinates and relationship expansion)
                MATCH (lastNode:City {id: lastNodeId})

                // 4. Expand one hop along outgoing ROUTE relationships
                MATCH (lastNode)-[route:ROUTE]->(nextNode:City)

                // 5. *** Beam Search Core Logic ***
                //    a) Prevent cycles within the *current* path being explored
                WHERE NOT nextNode.id IN currentPath.nodeIds
                //    b) Ensure the next node is in a selected country (if applicable)
                AND EXISTS { MATCH (nextNode)-[:OF_COUNTRY]->(c:Country) WHERE c.id IN $selectedCountryIds }

                // --- Calculate Incremental Scores for the new hop ---

                // 6. Incremental Attribute Score for nextNode
                // Using CALL subquery for clarity/isolation
                CALL {
                    WITH nextNode // Pass only necessary data
                    MATCH (nextNode)-[:HAS_METRIC]->(m:Metric)
                    WITH m.criteria AS criteria, m.metric AS metric,
                        // Apply user preference weighting
                        CASE m.criteria
                            WHEN 'FOOD' THEN m.metric * ($foodPreference / 3.0)
                            WHEN 'NIGHTLIFE' THEN m.metric * ($nightlifePreference / 3.0)
                            WHEN 'SAILING' THEN m.metric * ($sailingPreference / 3.0)
                            ELSE 0.0 // Should not happen if criteria are fixed
                        END AS weightedMetric
                    RETURN sum(weightedMetric) AS incrementalAttributeScore
                }

                // 7. Incremental Cost Penalty for the new route
                WITH currentPath, lastNode, route, nextNode, incrementalAttributeScore,
                    CASE $costPreference
                        WHEN 1 THEN (toFloat(route.averageCost) / 25.0) // High penalty scaling
                        WHEN 2 THEN (toFloat(route.averageCost) / 50.0) // Moderate penalty scaling
                        ELSE 0.0 // No penalty (preference 3)
                    END AS incrementalCostPenalty

                // 8. Incremental Popularity for the new route
                WITH currentPath, lastNode, route, nextNode, incrementalAttributeScore, incrementalCostPenalty,
                    route.popularity AS incrementalPopularity

                // 9. Incremental Distance for the new route
                // Ensure coordinates exist before calculating distance
                WITH currentPath, lastNode, route, nextNode, incrementalAttributeScore, incrementalCostPenalty, incrementalPopularity,
                    CASE
                        WHEN lastNode.coordinate IS NOT NULL AND nextNode.coordinate IS NOT NULL
                        THEN point.distance(lastNode.coordinate, nextNode.coordinate) / 1000.0
                        ELSE 0.0 // Assign 0 distance if coordinates are missing, or handle as error
                    END AS incrementalDistanceKm


                // --- Calculate NEW Total Scores ---
                WITH currentPath, route, nextNode,
                    // Add incremental scores to the scores from the previous path state
                    currentPath.scores.totalAttributeScore + incrementalAttributeScore AS newTotalAttributeScore,
                    currentPath.scores.totalCostPenalty + incrementalCostPenalty AS newTotalCostPenalty,
                    currentPath.scores.totalPopularity + incrementalPopularity AS newTotalPopularity,
                    currentPath.scores.totalDistanceKm + incrementalDistanceKm AS newTotalDistanceKm

                // 10. Calculate the new FINAL score for the extended path
                WITH currentPath, route, nextNode,
                    newTotalAttributeScore, newTotalCostPenalty, newTotalPopularity, newTotalDistanceKm,
                    // Apply weights to the new total scores
                    (newTotalAttributeScore * $attributeWeight)
                    - newTotalCostPenalty
                    + (newTotalPopularity * $popularityWeight)
                    - (newTotalDistanceKm * $distanceWeight)
                    AS newFinalScore

                // --- Prepare the output for the next iteration ---

                // 11. Construct the state object for the extended path
                WITH currentPath, route, nextNode, newFinalScore,
                    // Append new node/rel IDs
                    currentPath.nodeIds + nextNode.id AS newNodeIds,
                    currentPath.relIds + elementId(route) AS newRelIds, // Use elementId()
                    // Store the new scores
                    {
                        totalAttributeScore: newTotalAttributeScore,
                        totalCostPenalty: newTotalCostPenalty,
                        totalPopularity: newTotalPopularity,
                        totalDistanceKm: newTotalDistanceKm,
                        finalScore: newFinalScore // Keep final score for ordering
                    } AS newScores

                // 12. *** Beam Search Pruning ***
                // Order all expanded paths from this hop by score and take the top N (beamWidth)
                ORDER BY newFinalScore DESC
                LIMIT $beamWidth

                // 13. Return the state of the best paths found at this hop
                RETURN
                    newNodeIds,
                    newRelIds,
                    newScores

                """)
                .bindAll(params)
                .fetchAs(Record.class)
                .mappedBy((TypeSystem typeSystem, Record record) -> {

                    return record;
                })
                .all().iterator();
            return results;

        } catch (Exception e) {
            log.error("Unexpected exception when trying to beam search in Neo4j. Exception: {}", e.getMessage());
            throw new Neo4jGenericException("Unexpected exception when trying to beam search in Neo4j.", e);
        }
    }

    public static String QUERY_RETURN_ALL_RELATIONSHIPS = """
            OPTIONAL MATCH (city)-[:OF_COUNTRY]->(country)

            WITH city, country
            OPTIONAL MATCH (city)-[:HAS_METRIC]->(m:Metric)
            WITH city, country, collect(m) as cityMetrics
           
            OPTIONAL MATCH (city)-[route:ROUTE]->(t)
           
            OPTIONAL MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country)
           
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
            """;

    public List<Neo4jCity> findByIds(List<String> ids, boolean includeRoutes) {
        List<Neo4jCity> cities = neo4jClient
                .query("""
                    MATCH (city:City) 
                    WHERE city.id in $ids
                       """
                       + QUERY_RETURN_ALL_RELATIONSHIPS)
                .bind(ids).to("ids")
                .fetchAs(Neo4jCity.class)
                .mappedBy((typeSystem, record) -> {
                    log.info("in here sonny");
                    if (includeRoutes) {
                        return cityWithAllRelationshipsMapper.apply(typeSystem, record);
                    }
                    return cityWithNoRoutesMapper.apply(typeSystem, record);

                }).all().stream().collect(Collectors.toList());
            
        return cities;
    }
    
}
