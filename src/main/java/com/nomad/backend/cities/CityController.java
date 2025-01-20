package com.nomad.backend.cities;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cities")
public class CityController {


    private CityRepository cityRepository;

    @Autowired
    public CityController(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }
//    @GetMapping
//    public ResponseEntity<List<City>> getCities(@RequestParam(value = "countryId") UUID countryId) {
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(repository.getCities(countryId));
//    }
}

