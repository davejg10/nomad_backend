package com.nomad.backend;

import com.nomad.backend.cities.*;
import com.nomad.backend.country.Country;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@Log4j2
class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(CityService cityService, CityRepository cityRepository) {

        return args -> {
//            String countryName = "CountryA";
//            Random random = new Random();
//
//            List<CityMetrics> cityMetrics = IntStream.range(0, 5).boxed()
//                    .map(i  -> new CityMetrics(
//                            new CityMetric(CityCriteria.SAILING, random.nextInt(11)),
//                            new CityMetric(CityCriteria.FOOD, random.nextInt(11)),
//                            new CityMetric(CityCriteria.NIGHTLIFE, random.nextInt(11))
//                    ))
//                    .toList();
//
//
//            City cityA = City.of("CityA", "", countryName, cityMetrics.get(0), Set.of());
//            City cityB = City.of("CityB", "", countryName, cityMetrics.get(1), Set.of());
//            City cityC = City.of("CityC", "", countryName, cityMetrics.get(2), Set.of());
//            City cityD = City.of("CityD", "", countryName, cityMetrics.get(3), Set.of());
//            City cityE = City.of("CityE", "", countryName, cityMetrics.get(4), Set.of());
//
//            cityA = cityA.addRoute(cityB, 3, 2, TransportType.BUS);
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
//
//            cityService.createOrUpdateCity(cityA);
//            cityService.createOrUpdateCity(cityB);
//            cityService.createOrUpdateCity(cityC);
//            cityService.createOrUpdateCity(cityD);
//            cityService.createOrUpdateCity(cityE);

            log.warn(cityRepository.findByNameReturnRoutes("CityA"));


        };
    }
}