package com.nomad.backend.city;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nomad.backend.city.domain.City;
import com.nomad.backend.exceptions.NotFoundRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class CityService {

    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;

    private CityService(ObjectMapper objectMapper, CityRepository cityRepository) {
        this.objectMapper = objectMapper;
        this.cityRepository = cityRepository;
    }

    public City getCity(String id, boolean includeNextHops) throws NotFoundRequestException {
        log.info("Fetching city with ID: {}, include next hops: {}", id, includeNextHops);
        Optional<City> city = includeNextHops ? cityRepository.findByIdFetchRoutes(id) : cityRepository.findById(id);

        if (city.isPresent()) {
            return city.get();
        } else {
            log.warn("City with ID {} not found.", id);
            throw new NotFoundRequestException("The city with id: " + id + " was not found");
        }
    }

    public void createOrUpdateCity(City city) throws Exception {
        log.info("Creating city: {}", city);
        cityRepository.saveCityWithDepth0(city.mapifyCity());
    }
}