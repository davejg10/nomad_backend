package com.nomad.backend.city;

import com.nomad.data_library.domain.neo4j.Neo4jCity;
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

    public Neo4jCity getCity(String id, boolean includeRoutes) throws NotFoundRequestException {
        log.info("Fetching Neo4jCity with ID: {}, include next hops: {}", id, includeRoutes);
        Optional<Neo4jCity> neo4jCity = includeRoutes ? neo4jCityRepository.findByIdFetchRoutes(id) : neo4jCityRepository.findById(id);

        if (neo4jCity.isPresent()) {
            return neo4jCity.get();
        } else {
            log.warn("Neo4jCity with ID {} not found.", id);
            throw new NotFoundRequestException("The Neo4jCity with id: " + id + " was not found");
        }
    }

    public Neo4jCity getCityFetchRoutesWithCountryId(String id, String routesCountryId) throws NotFoundRequestException {
        log.info("Fetching Neo4jCity with ID {}, only including routes with cities of Neo4jCountry ID: {}", id, routesCountryId);
        Optional<Neo4jCity> neo4jCity = neo4jCityRepository.findByIdFetchRoutesByCountryId(id, routesCountryId);

        if (neo4jCity.isPresent()) {
            return neo4jCity.get();
        } else {
            log.warn("Neo4jCity with ID {} not found.", id);
            throw new NotFoundRequestException("The Neo4jCity with ID: " + id + " was not found");
        }
    }

    public void createOrUpdateCity(Neo4jCity city) {
        log.info("Creating Neo4jCity: {}", city);
        neo4jCityRepository.saveNeo4jCityWithDepth0(city);
    }
}