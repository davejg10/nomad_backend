package com.nomad.backend.city.neo4j;

import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.backend.exceptions.NotFoundRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class Neo4jCityService {

    private final Neo4jCityRepository neo4jCityRepository;

    private Neo4jCityService(Neo4jCityRepository neo4jCityRepository) {
        this.neo4jCityRepository = neo4jCityRepository;
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

    public Set<Neo4jRoute> fetchRoutesByTargetCityCountryIdOrderByPreferences(String id, String targetCityCountryId, Map<String, String> cityCriteriaPreferences, int costPreference) throws NotFoundRequestException {
        log.info("Fetching Neo4jCity with ID {}, only including routes with cities of Neo4jCountry ID: {}. Returning set of routes ordered by preferences.", id, targetCityCountryId);
        Set<Neo4jRoute> orderedRoutes = neo4jCityRepository.fetchRoutesByTargetCityCountryIdOrderByPreferences(id, targetCityCountryId, cityCriteriaPreferences, costPreference);
        if (orderedRoutes.isEmpty()) {
            log.warn("There are no routes for city with Id: {}, with targetCityCountryId: {}. Returning empty set.", id, targetCityCountryId);
        }
        return orderedRoutes;

    }
}