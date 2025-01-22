package com.nomad.backend.country;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/countries")
public class CountryController {

    @Autowired
    private CountryRepository repository;

    @GetMapping
    public ResponseEntity<List<Country>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(repository.getAll());
    }
}