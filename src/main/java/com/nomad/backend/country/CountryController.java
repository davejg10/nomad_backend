package com.nomad.backend.country;

import com.nomad.backend.city.domain.City;
import com.nomad.backend.country.domain.Country;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public ResponseEntity<Set<Country>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(countryService.findAllCountries());
    }

    @GetMapping(path={"/{countryId}/cities"})
    public ResponseEntity<Set<City>> getCitiesGivenCountry(@PathVariable String countryId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(countryService.getCitiesGivenCountry(countryId));
    }
}