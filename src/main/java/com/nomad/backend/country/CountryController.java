package com.nomad.backend.country;

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

    @GetMapping(path={"/{countryName}"})
    public ResponseEntity<Country> getCountry(
            @PathVariable String countryName,
            @RequestParam(defaultValue = "false") boolean returnAllCities) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(countryService.getCountryByName(countryName, returnAllCities));
    }
}