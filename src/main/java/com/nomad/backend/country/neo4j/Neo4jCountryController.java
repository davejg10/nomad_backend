package com.nomad.backend.country.neo4j;

import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/neo4jCountries")
public class Neo4jCountryController {

    private final Neo4jCountryService neo4jCountryService;

    public Neo4jCountryController(Neo4jCountryService neo4jCountryService) {
        this.neo4jCountryService = neo4jCountryService;
    }

    @GetMapping
    public ResponseEntity<Set<Neo4jCountry>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCountryService.findAllCountries());
    }

    @GetMapping(path={"/{countryId}/cities"})
    public ResponseEntity<Set<Neo4jCity>> getCitiesGivenCountry(@PathVariable String countryId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(neo4jCountryService.getCitiesGivenCountry(countryId));
    }
}