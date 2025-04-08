package com.nomad.backend.city.sql;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nomad.data_library.domain.sql.SqlCity;

@RestController
@RequestMapping("/sqlCities")
public class SqlCityController {

    private final SqlCityService sqlCityService;

    public SqlCityController(SqlCityService sqlCityService) {
        this.sqlCityService = sqlCityService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SqlCity> findById(
            @PathVariable String id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sqlCityService.findById(id));
    }
}


    