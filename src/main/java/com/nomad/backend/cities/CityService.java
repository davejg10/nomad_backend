package com.nomad.backend.cities;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
public class CityService {

    private final CityRepository cityRepository;

    @Autowired
    private CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public void createOrUpdateCity(City city) throws Exception {
        if (!city.getRoutes().isEmpty()) {
            city = syncTargetCities(city);
        }
        city = syncSourceCity(city);
        cityRepository.saveCityWithDepth0(mapifyCity(city));
    }

    public City syncSourceCity(City city) {
        Optional<City> existingCity = cityRepository.findByName(city.getName());
        if (existingCity.isPresent()) {
            city = city.withId(existingCity.get().getId());
        }
        return city;
    }

    public City syncTargetCities(City city) {
        Set<String> targetCitiesToSync = city.getRoutes().stream()
                .filter(route -> route.getTargetCity().getId() == null)
                .map(route -> route.getTargetCity().getName())
                .collect(Collectors.toSet());

        if (!targetCitiesToSync.isEmpty()) {
            Set<City> existingTargetCities = cityRepository.findByNameIn(targetCitiesToSync);

            if (!existingTargetCities.isEmpty()) {
                Map<String, City> existingTargetCitiesMap = existingTargetCities.stream()
                        .collect(Collectors.toMap(City::getName, Function.identity()));

                Set<Route> syncedRoutes = new HashSet<>(city.getRoutes().size());

                for (Route route : city.getRoutes()) {
                    String targetCityName = route.getTargetCity().getName();
                    if (existingTargetCitiesMap.containsKey(targetCityName)) {
                        Route syncedRoute =  Route.of(existingTargetCitiesMap.get(targetCityName), route.getPopularity(), route.getWeight(), route.getTransportType());
                        syncedRoutes.add(syncedRoute);
                        continue;
                    }
                    syncedRoutes.add(route);
                }
                return City.of(city.getName(), city.getDescription(), city.getCountryName(), city.getCityMetrics(), syncedRoutes);
            }
        }
        return city;
    }

    public Map<String, Object> mapifyCity(City cityToCreateOrUpdate) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cityToCreateOrUpdate, Map.class);
    }
}