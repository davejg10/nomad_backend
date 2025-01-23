package com.nomad.backend;

import com.nomad.backend.city.*;
import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.domain.Country;
import com.nomad.backend.country.CountryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

@Configuration
@Log4j2
class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(CityService cityService, CityRepository cityRepository, CountryRepository countryRepository) {

        return args -> {
            Country countryA = Country.of("CountryA", "", Set.of());
            countryRepository.saveCountryWithDepth0(countryA);

            Random random = new Random();

            List<CityMetrics> cityMetrics = IntStream.range(0, 5).boxed()
                    .map(i  -> new CityMetrics(
                            new CityMetric(CityCriteria.SAILING, random.nextInt(11)),
                            new CityMetric(CityCriteria.FOOD, random.nextInt(11)),
                            new CityMetric(CityCriteria.NIGHTLIFE, random.nextInt(11))
                    ))
                    .toList();


            City cityA = City.of("CityA", "", cityMetrics.get(0), Set.of(), countryA);
            City cityB = City.of("CityB", "", cityMetrics.get(1), Set.of(), countryA);
//            City cityC = City.of("CityC", "", cityMetrics.get(2), Set.of(), countryA);
//            City cityD = City.of("CityD", "", cityMetrics.get(3), Set.of(), countryA);
//            City cityE = City.of("CityE", "", cityMetrics.get(4), Set.of(), countryA);
//
            cityA = cityA.addRoute(cityB, 4, 2, TransportType.BUS);
            cityA = cityA.addRoute(cityB, 3, 2, TransportType.FLIGHT);

//
//            cityB = cityB.addRoute(cityC, 2, 2, TransportType.FLIGHT);
//            cityB = cityB.addRoute(cityC, 2, 3, TransportType.BUS);
//            cityB = cityB.addRoute(cityD, 1, 2, TransportType.BUS);
//
//            cityC = cityC.addRoute(cityE, 1, 1, TransportType.BUS);
//            cityC = cityC.addRoute(cityD, 1, 2, TransportType.BUS);
//
//            cityD = cityD.addRoute(cityE, 0, 1, TransportType.BUS);
//
//            cityE = cityE.addRoute(cityC, 1, 1, TransportType.BUS);

            cityService.createOrUpdateCity(cityA);
//            cityService.createOrUpdateCity(cityB);
//            cityService.createOrUpdateCity(cityC);
//            cityService.createOrUpdateCity(cityD);
//            cityService.createOrUpdateCity(cityE);
//
//            log.warn(cityRepository.findByNameReturnRoutes("CityA"));


        };
    }
}