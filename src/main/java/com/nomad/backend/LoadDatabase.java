package com.nomad.backend;

import com.nomad.backend.cities.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@Log4j2
class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(CityService cityService) {

        return args -> {
            String countryName = "Thailand";


            City bangkok = City.of(
                "Bangkok",
                "mkomo",
                countryName,
                Set.of()
//                    new CityMetric(CityCriteria.SAILING, 8),
//                    new CityMetric(CityCriteria.FOOD, 8),
//                    new CityMetric(CityCriteria.NIGHTLIFE, 8)
            );

            City phuket = City.of(
                "Phuket",
                "",
                countryName,
                Set.of()
//                            new CityMetric(CityCriteria.SAILING, 4),
//                            new CityMetric(CityCriteria.FOOD, 5),
//                            new CityMetric(CityCriteria.NIGHTLIFE, 10)
            );

            City pai = City.of(
                    "Pai",
                    "",
                    countryName,
                    Set.of()
//                            new CityMetric(CityCriteria.SAILING, 4),
//                            new CityMetric(CityCriteria.FOOD, 5),
//                            new CityMetric(CityCriteria.NIGHTLIFE, 10)
            );


//            pai = pai.addRoute(phuket, 2, 1, TransportType.BUS);

            bangkok = bangkok.addRoute(phuket, "2", "2", TransportType.BUS);
            phuket = phuket.addRoute(pai, "2", "1", TransportType.PLANE);
            pai = pai.addRoute(bangkok, "2", "4", TransportType.PLANE);
            pai = pai.addRoute(bangkok, "2", "3", TransportType.PLANE);
            pai = pai.addRoute(bangkok, "2", "4", TransportType.BUS);

            City hanoi = City.of(
                    "Hanoi",
                    "",
                    "Vietnam",
                    Set.of()
//                            new CityMetric(CityCriteria.SAILING, 4),
//                            new CityMetric(CityCriteria.FOOD, 5),
//                            new CityMetric(CityCriteria.NIGHTLIFE, 10)
            );

            cityService.createOrUpdateCity(bangkok);
            cityService.createOrUpdateCity(phuket);
            cityService.createOrUpdateCity(pai);
            cityService.createOrUpdateCity(hanoi);
//            bangkok.addRoute(phuket, 2, 2, TransportType.BUS);


//            cityService.createOrUpdateCity(bangkok);


        };
    }
}