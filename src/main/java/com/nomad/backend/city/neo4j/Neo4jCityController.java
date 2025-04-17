package com.nomad.backend.city.neo4j;

import com.nomad.backend.domain.CityInfoDTO;
import com.nomad.backend.domain.RouteInfoDTO;
import com.nomad.data_library.domain.neo4j.Neo4jCity;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    //TODO refactor this 
    @GetMapping("/routes/preferences")
    public ResponseEntity<Set<CityInfoDTO>> fetchCitiesByCountryIdsOrderByPreferences(
            @RequestParam String selectedCountryIds,
            @RequestParam Map<String, String> cityCriteriaPreferences,
            @RequestParam int costPreference) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCityService.fetchCitiesByCountryIdsOrderByPreferences(selectedCountryIds, cityCriteriaPreferences, costPreference));
    }
    
    @GetMapping("/{id}/routes/preferences")
    public ResponseEntity<Set<RouteInfoDTO>> fetchRoutesByCityIdAndCountryIdsOrderByPreferences(
            @PathVariable String id,
            @RequestParam String selectedCountryIds,
            @RequestParam Map<String, String> cityCriteriaPreferences,
            @RequestParam int costPreference) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCityService.fetchRoutesByCityIdAndCountryIdsOrderByPreferences(id, selectedCountryIds, cityCriteriaPreferences, costPreference));
    }

    @GetMapping("/{id}/beam")
    public ResponseEntity<List<Map<String, Object>>> beamSearchWithPreferences(
            @PathVariable String id,
            @RequestParam String selectedCountryIds,
            @RequestParam Map<String, String> cityCriteriaPreferences,
            @RequestParam int costPreference,
            @RequestParam int maxHops,
            @RequestParam int beamWidth) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCityService.beamApproachBoy(id, selectedCountryIds, cityCriteriaPreferences, costPreference, maxHops, beamWidth));
    }
}