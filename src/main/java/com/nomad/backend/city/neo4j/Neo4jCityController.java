package com.nomad.backend.city.neo4j;

import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nomad.data_library.domain.CityCriteria;

@RestController
@RequestMapping("/neo4jCities")
public class Neo4jCityController {

    private final Neo4jCityService neo4jCityService;

    public Neo4jCityController(Neo4jCityService neo4jCityService) {
        this.neo4jCityService = neo4jCityService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Neo4jCity> findById(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeRoutes) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCityService.findById(id, includeRoutes));
    }

    @GetMapping("/{id}/routes")
    public ResponseEntity<Neo4jCity> findByIdFetchRoutesByTargetCityCountryId(
            @PathVariable String id,
            @RequestParam String targetCityCountryId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCityService.findByIdFetchRoutesByTargetCityCountryId(id, targetCityCountryId));
    }
    
    @GetMapping("/{id}/routes/preferences")
    public ResponseEntity<Set<Neo4jRoute>> fetchRoutesByTargetCityCountryIdOrderByPreferences(
            @PathVariable String id,
            @RequestParam String targetCityCountryId,
            @RequestParam Map<String, String> cityCriteriaPreferences,
            @RequestParam int costPreference) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCityService.fetchRoutesByTargetCityCountryIdOrderByPreferences(id, targetCityCountryId, cityCriteriaPreferences, costPreference));
    }
}