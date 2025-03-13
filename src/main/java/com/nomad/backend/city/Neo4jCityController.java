package com.nomad.backend.city;

import com.nomad.data_library.domain.neo4j.Neo4jCity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/neo4jCities")
public class Neo4jCityController {

    private final Neo4jCityService neo4jCityService;

    public Neo4jCityController(Neo4jCityService neo4jCityService) {
        this.neo4jCityService = neo4jCityService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Neo4jCity> getNeo4jCity(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeRoutes) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCityService.getCity(id, includeRoutes));
    }

    @GetMapping("/{id}/routes/{routesCountryId}")
    public ResponseEntity<Neo4jCity> getCityFetchRoutesWithCountryId(
            @PathVariable String id,
            @PathVariable String routesCountryId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCityService.getCityFetchRoutesWithCountryId(id, routesCountryId));
    }
}

