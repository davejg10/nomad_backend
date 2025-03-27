package com.nomad.backend.routes.sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nomad.backend.domain.CityDTO;
import com.nomad.data_library.domain.sql.RouteInstance;

@RestController
@RequestMapping("/route-instances")
public class SqlRouteInstanceController {

    private final SqlRouteInstanceService sqlRouteInstanceService;

    public SqlRouteInstanceController(SqlRouteInstanceService sqlRouteInstanceService) {
        this.sqlRouteInstanceService = sqlRouteInstanceService;
    }
    
    @GetMapping()
    private ResponseEntity<List<RouteInstance>> findByRouteDefinitionIdInAndSearchDate(
            @RequestParam List<UUID> routeDefinitionIds,
            @RequestParam LocalDate searchDate,
            @RequestParam String sourceCityId,
            @RequestParam String sourceCityName, 
            @RequestParam String targetCityId,
            @RequestParam String targetCityName,
            @RequestParam int attempt) {
        CityDTO sourceCity = new CityDTO(sourceCityId, sourceCityName);
        CityDTO targetCity = new CityDTO(targetCityId, targetCityName);

        Optional<List<RouteInstance>> routeInstances = sqlRouteInstanceService.findByRouteDefinitionIdInAndSearchDate(sourceCity, targetCity, routeDefinitionIds, searchDate, attempt);

        if (routeInstances.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(routeInstances.get());
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
    }
}
