package com.nomad.backend.cities;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

        System.out.println();
        log.warn("in createOrUpdateCity with city: " + city);

        if (!city.getRoutes().isEmpty()) {
            city = syncAllRoutes(city);
        } else {
            log.warn("Cities routes are empty, no routes to sync");
        }

        Optional<City> syncedCity = cityRepository.findByNameReturnRoutes(city.getName());
        log.warn("Quieried findByNameReturnRoutes returned: " + syncedCity);
        if (syncedCity.isPresent()) {
            City existingCity = syncedCity.get();
            for (Route route : city.getRoutes()) {
                existingCity = existingCity.addRoute(route);
            }
            cityRepository.save(existingCity);
        } else {
            log.info("City does not exist, adding city: " + city);
            cityRepository.save(city);
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