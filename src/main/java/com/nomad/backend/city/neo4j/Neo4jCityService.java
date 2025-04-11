package com.nomad.backend.city.neo4j;

import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.backend.domain.CityInfoDTO;
import com.nomad.backend.domain.RouteInfoDTO;
import com.nomad.backend.exceptions.NotFoundRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
}