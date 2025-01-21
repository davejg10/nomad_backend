package com.nomad.backend.cities;

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

    record RouteDTO(String targetCityName, int popularity, int weight, TransportType transportType) {}

    public void createOrUpdateCity(City city) throws Exception {

        System.out.println();
        log.warn("in createOrUpdateCity with city: " + city);

        if (!city.getRoutes().isEmpty()) {
            city = syncAllRoutes(city);
        } else {
            log.warn("Cities routes are empty, no routes to sync");
        }
        log.warn("About to fetch sync City with database city: " + city);



        Optional<City> syncedCity = cityRepository.findByNameReturnRoutes(city.getName());
        log.warn("Quieried findByNameReturnRoutes returned: " + syncedCity);
        if (syncedCity.isPresent()) {
            City existingCity = syncedCity.get();
            for (Route route : city.getRoutes()) {
                existingCity = existingCity.addRoute(route);
            }
            Map<String, String> cityMap = Map.of(
                    "name", existingCity.getName(),
                    "description", existingCity.getDescription(),
                    "countryName", existingCity.getCountryName()
            );
            List<Map<String, String>> routeMaps = existingCity.getRoutes().stream()
                    .map(route -> Map.of(
                            "targetCityName", route.getTargetCity().getName(),
                            "targetCityDescription", route.getTargetCity().getDescription(),
                            "targetCityCountryName", route.getTargetCity().getCountryName(),
                            "popularity", route.getPopularity(),
                            "weight", route.getWeight(),
                            "transportType", route.getTransportType().toString()
                    ))
                    .collect(Collectors.toList());
            log.warn("Saving the following to neo4j:");
            log.warn(routeMaps);
            cityRepository.saveCityDepth0(cityMap, routeMaps);
        } else {
            log.info("City does not exist, adding city: " + city);

            Map<String, String> cityMap = Map.of(
                    "name", city.getName(),
                    "description", city.getDescription(),
                    "countryName", city.getCountryName()
            );

            List<Map<String, String>> routeMaps = city.getRoutes().stream()
                    .map(route -> Map.of(
                            "targetCityName", route.getTargetCity().getName(),
                            "targetCityDescription", route.getTargetCity().getDescription(),
                            "targetCityCountryName", route.getTargetCity().getCountryName(),
                            "popularity", String.valueOf(route.getPopularity()),
                            "weight", String.valueOf(route.getWeight()),
                            "transportType", route.getTransportType().toString()
                    ))
                    .collect(Collectors.toList());
            cityRepository.saveCityDepth0(cityMap, routeMaps);

        }
    }

    public City syncAllRoutes(City city) {


        log.warn("in syncAllRoutes with city: " + city);

        Set<String> targetCitiesToSync = city.getRoutes().stream()
                .filter(route -> route.getTargetCity().getId() == null)
                .map(route -> route.getTargetCity().getName())
                .collect(Collectors.toSet());

        log.warn("The following target city names contained null ids: " + targetCitiesToSync);
        if (!targetCitiesToSync.isEmpty()) {
            Set<City> syncedTargetCities = cityRepository.findByNameIn(targetCitiesToSync);

            log.warn("Synced cities from DB: " + syncedTargetCities);
            if (!syncedTargetCities.isEmpty()) {
                Map<String, City> syncedTargetCitiesMap = syncedTargetCities.stream()
                        .collect(Collectors.toMap(City::getName, Function.identity()));

                Set<Route> syncedRoutes = new HashSet<>(city.getRoutes().size());

                for (Route route : city.getRoutes()) {
                    String targetCityName = route.getTargetCity().getName();
                    if (syncedTargetCitiesMap.containsKey(targetCityName)) {
                        Route syncedRoute =  Route.of(syncedTargetCitiesMap.get(targetCityName), route.getPopularity(), route.getWeight(), route.getTransportType());
                        syncedRoutes.add(syncedRoute);
                        log.warn(targetCityName + " had null id therefore re-constructing route with id fetched from db, new route: " + syncedRoute);
                        continue;
                    }
                    syncedRoutes.add(route);
                }
                return City.of(city.getName(), city.getDescription(), city.getCountryName(), syncedRoutes);

            }
            log.warn("None of the cities existed in the database therefore returning City with null references");
        }
        log.warn("There were no cities to sync because no target cities had null id therefore returning city");
        return city;
    }
}