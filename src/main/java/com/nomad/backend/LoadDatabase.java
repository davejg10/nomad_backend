package com.nomad.backend;

import com.nomad.backend.city.*;
import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.CountryRepository;
import com.nomad.backend.country.domain.Country;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
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
            Country countryB = Country.of("B", "", Set.of());

            countryRepository.saveCountryWithDepth0(countryA);
            countryRepository.saveCountryWithDepth0(countryB);


            Random random = new Random();

            List<CityMetrics> cityMetrics = IntStream.range(0, 5).boxed()
                    .map(i  -> new CityMetrics(
                            new CityMetric(CityCriteria.SAILING, random.nextDouble(11)),
                            new CityMetric(CityCriteria.FOOD, random.nextDouble(11)),
                            new CityMetric(CityCriteria.NIGHTLIFE, random.nextDouble(11))
                    ))
                    .toList();

            City cityA = City.of("CityA", "", cityMetrics.get(0), Set.of(), countryA);
            City cityB = City.of("CityB", "", cityMetrics.get(1), Set.of(), countryA);
            City cityC = City.of("CityC", "", cityMetrics.get(2), Set.of(), countryB);
            City cityD = City.of("CityD", "", cityMetrics.get(3), Set.of(), countryA);
            City cityE = City.of("CityE", "", cityMetrics.get(4), Set.of(), countryA);

            cityA = cityA.addRoute(cityB, 3.0, 2.5, TransportType.BUS);

            cityB = cityB.addRoute(cityC, 2.0, 2.4, TransportType.FLIGHT);
            cityB = cityB.addRoute(cityC, 2.0, 3.0, TransportType.BUS);
            cityB = cityB.addRoute(cityD, 1.0, 2.2, TransportType.BUS);

            cityC = cityC.addRoute(cityE, 1.0, 1.4, TransportType.BUS);
            cityC = cityC.addRoute(cityD, 1.0, 2.3, TransportType.BUS);

            cityD = cityD.addRoute(cityE, 0.0, 1.3, TransportType.BUS);

            cityE = cityE.addRoute(cityC, 1.0, 1.0, TransportType.BUS);

            cityService.createOrUpdateCity(cityA);
            cityService.createOrUpdateCity(cityB);

            cityService.createOrUpdateCity(cityB);
            cityService.createOrUpdateCity(cityC);
            cityService.createOrUpdateCity(cityD);
            cityService.createOrUpdateCity(cityE);

        };
    }
}