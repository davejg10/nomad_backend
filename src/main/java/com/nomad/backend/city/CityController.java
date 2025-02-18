package com.nomad.backend.city;


import com.nomad.backend.city.domain.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cities")
public class CityController {

    private final CityService cityService;

    @Autowired
    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<City> getCity(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeRoutes) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cityService.getCity(id, includeRoutes));
    }

    @GetMapping("/{id}/routes/{routesCountryId}")
    public ResponseEntity<City> getCityFetchRoutesWithCountryId(
            @PathVariable String id,
            @PathVariable String routesCountryId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cityService.getCityFetchRoutesWithCountryId(id, routesCountryId));
    }
}

