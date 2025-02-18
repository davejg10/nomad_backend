package com.nomad.backend.city;

import com.nomad.backend.city.domain.City;
import com.nomad.backend.exceptions.NotFoundRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class CityService {

    private final CityRepository cityRepository;

    private CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public City getCity(String id, boolean includeRoutes) throws NotFoundRequestException {
        log.info("Fetching city with ID: {}, include next hops: {}", id, includeRoutes);
        Optional<City> city = includeRoutes ? cityRepository.findByIdFetchRoutes(id) : cityRepository.findById(id);

        if (city.isPresent()) {
            return city.get();
        } else {
            log.warn("City with ID {} not found.", id);
            throw new NotFoundRequestException("The city with id: " + id + " was not found");
        }
    }

    public City getCityFetchRoutesWithCountryId(String id, String routesCountryId) throws NotFoundRequestException {
        log.info("Fetching city with ID {}, only including routes with cities of country ID: {}", id, routesCountryId);
        Optional<City> city = cityRepository.findByIdFetchRoutesByCountryId(id, routesCountryId);

        if (city.isPresent()) {
            return city.get();
        } else {
            log.warn("City with ID {} not found.", id);
            throw new NotFoundRequestException("The city with ID: " + id + " was not found");
        }
    }

    public void createOrUpdateCity(City city) {
        log.info("Creating city: {}", city);
        cityRepository.saveCityWithDepth0(city);
    }
}