package com.nomad.backend.city.neo4j;

import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.CityMetric;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.domain.CityInfoDTO;
import com.nomad.backend.domain.RouteInfoDTO;
import com.nomad.backend.exceptions.NotFoundRequestException;
import lombok.extern.log4j.Log4j2;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class Neo4jCityService {

    private final ObjectMapper objectMapper;
    private final Neo4jCityRepository neo4jCityRepository;

    private Neo4jCityService(Neo4jCityRepository neo4jCityRepository, ObjectMapper objectMapper) {
        this.neo4jCityRepository = neo4jCityRepository;
        this.objectMapper = objectMapper;
    }

    public Neo4jCity findById(String id, boolean includeRoutes) throws NotFoundRequestException {
        log.info("Fetching Neo4jCity with ID: {}, include next hops: {}", id, includeRoutes);
        Optional<Neo4jCity> neo4jCity = includeRoutes ? neo4jCityRepository.findByIdFetchRoutes(id) : neo4jCityRepository.findById(id);

        if (neo4jCity.isPresent()) {
            return neo4jCity.get();
        } else {
            log.warn("Neo4jCity with ID {} not found.", id);
            throw new NotFoundRequestException("The Neo4jCity with id: " + id + " was not found");
        }
    }

    public Neo4jCity findByIdFetchRoutesByTargetCityCountryId(String id, String targetCityCountryId) throws NotFoundRequestException {
        log.info("Fetching Neo4jCity with ID {}, only including routes with cities of Neo4jCountry ID: {}", id, targetCityCountryId);
        Optional<Neo4jCity> neo4jCity = neo4jCityRepository.findByIdFetchRoutesByTargetCityCountryId(id, targetCityCountryId);

        if (neo4jCity.isPresent()) {
            return neo4jCity.get();
        } else {
            log.warn("Neo4jCity with ID {} not found.", id);
            throw new NotFoundRequestException("The Neo4jCity with ID: " + id + " was not found");
        }
    }

    public Set<CityInfoDTO> fetchCitiesByCountryIdsOrderByPreferences(String selectedCountriesIds, Map<String, String> cityCriteriaPreferences, int costPreference) throws NotFoundRequestException {
        Set<String> targetCityCountryIds = Set.of(selectedCountriesIds.split(","));
        log.info("Fetching all Neo4jCities in the following Neo4jCountries: {}. Returning set of routes ordered by preferences.", targetCityCountryIds);

        Set<CityInfoDTO> orderedCities = neo4jCityRepository.fetchCitiesByCountryIdsOrderByPreferences(targetCityCountryIds, cityCriteriaPreferences, costPreference);
        if (orderedCities.isEmpty()) {
            log.warn("There were no cities found with selectedCountriesIds: {}. Returning empty set.", targetCityCountryIds);
        }
        return orderedCities;

    }

    public Set<RouteInfoDTO> fetchRoutesByCityIdAndCountryIdsOrderByPreferences(String id, String selectedCountriesIdsString, Map<String, String> cityCriteriaPreferences, int costPreference) throws NotFoundRequestException {
        Set<String> selectedCountriesIds = Set.of(selectedCountriesIdsString.split(","));
        log.info("Fetching Neo4jCity with ID {}, only including routes with cities in the following Neo4jCountries: {}. Returning set of routes ordered by preferences.", id, selectedCountriesIdsString);

        Set<RouteInfoDTO> orderedRoutes = neo4jCityRepository.fetchRoutesByCityIdAndCountryIdsOrderByPreferences(id, selectedCountriesIds, cityCriteriaPreferences, costPreference);
        if (orderedRoutes.isEmpty()) {
            log.warn("There are no routes for city with Id: {}, with selectedCountriesIds: {}. Returning empty set.", id, selectedCountriesIds);
        }
        return orderedRoutes;
    }

    public List<Map<String, Object>> beamApproachBoy(
        String id,
        String selectedCountriesIdsString,
        Map<String, String> cityCriteriaPreferences, 
        int costPreference,
        int maxHops,
        int beamWidth
        ) {
        Set<String> selectedCountriesIds = Set.of(selectedCountriesIdsString.split(","));

        Map<String, Object> initialScores = Map.of(
            "totalAttributeScore", 0.0,
            "totalCostPenalty", 0.0,
            "totalPopularity", 0.0,
            "totalDistanceKm", 0.0,
            "finalScore", 0.0 // Initial score is neutral
        );
        Map<String, Object> initialPathState = Map.of(
            "nodeIds", List.of(id),
            "relIds", List.of(), // Empty list of rels
            "scores", initialScores
        );
        List<Map<String, Object>> currentBeam = List.of(initialPathState);


        // Prepare parameters for the Cypher query
        Map<String, Object> params = new HashMap<>();
        params.put("currentPaths", currentBeam); // Pass the beam from the previous hop
        params.put("beamWidth", beamWidth);
        params.put("foodPreference", Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.FOOD.name()))); // Pass user inputs & weights
        params.put("nightlifePreference", Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.NIGHTLIFE.name())));
        params.put("sailingPreference", Integer.parseInt(cityCriteriaPreferences.get(CityCriteria.SAILING.name())));
        params.put("costPreference", costPreference);
        params.put("attributeWeight", 1.0);
        params.put("popularityWeight", 0.5);
        params.put("distanceWeight", 0.1);
        params.put("selectedCountryIds", selectedCountriesIds);

        for (int hop = 1; hop <= maxHops; hop++) {
            if (currentBeam.isEmpty()) {
                // No more paths could be found in the previous iteration
                break;
            }

            params.put("currentPaths", currentBeam);
            
            Iterator<Record> results = neo4jCityRepository.beamSearch(params);

            // Process the results into the beam for the *next* iteration
            List<Map<String, Object>> nextBeam = new ArrayList<>();
            while (results.hasNext()) {
                Record record = results.next();
                Map<String, Object> nextPathState = Map.of(
                    "nodeIds", record.get("newNodeIds").asList(Value::asString), // Adapt based on driver version/methods
                    "relIds", record.get("newRelIds").asList(Value::asString), // Relationship elementIds are strings
                    "scores", record.get("newScores").asMap()
                );
                nextBeam.add(nextPathState);
            }

            // Update the beam for the next loop iteration
            currentBeam = nextBeam;
            System.out.println("Hop " + hop + ": Found " + currentBeam.size() + " paths in beam.");
        }
        
        List<Map<String, Object>> newBeam = new ArrayList<>();
        for(int i =0; i<currentBeam.size(); i++) {
            Map<String, Object> routeSet = currentBeam.get(i);
            List<String> nodeIds = (List<String>) routeSet.get("nodeIds");
            List<Neo4jCity> fetchedCities = neo4jCityRepository.findByIds(nodeIds, false);
            routeSet = new HashMap<>(routeSet); // now mutable

            routeSet.put("neo4jCities", fetchedCities);
            newBeam.add(routeSet);
        }


        return newBeam;

    }
}