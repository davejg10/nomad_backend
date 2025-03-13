package com.nomad.backend;

import com.nomad.data_library.domain.*;
import com.nomad.backend.city.Neo4jCityRepository;
import com.nomad.backend.city.Neo4jCityService;
import com.nomad.backend.country.Neo4jCountryRepository;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;

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
    CommandLineRunner initDatabase(Neo4jCityService neo4jCityService, Neo4jCityRepository neo4jCityRepository, Neo4jCountryRepository neo4jCountryRepository) {

        return args -> {
            
            Neo4jCountry neo4jCountryA = Neo4jCountry.of("Thailand", Set.of());
            Neo4jCountry neo4jCountryB = Neo4jCountry.of("B", Set.of());

            neo4jCountryRepository.createCountry(neo4jCountryA);
//            neo4jCountryRepository.createCountry(neo4jCountryB);


            Random random = new Random();

            List<CityMetrics> Neo4jCityMetrics = IntStream.range(0, 5).boxed()
                    .map(i  -> new CityMetrics(
                            new CityMetric(CityCriteria.SAILING, random.nextDouble(11)),
                            new CityMetric(CityCriteria.FOOD, random.nextDouble(11)),
                            new CityMetric(CityCriteria.NIGHTLIFE, random.nextDouble(11))
                    ))
                    .toList();

            Neo4jCity neo4jCityA = Neo4jCity.of("Bangkok", Neo4jCityMetrics.get(0), Set.of(), neo4jCountryA);
            Neo4jCity neo4jCityB = Neo4jCity.of("Pai", Neo4jCityMetrics.get(1), Set.of(), neo4jCountryA);
            Neo4jCity neo4jCityC = Neo4jCity.of("Chiang-Mai", Neo4jCityMetrics.get(2), Set.of(), neo4jCountryA);
            Neo4jCity neo4jCityD = Neo4jCity.of("Phuket", Neo4jCityMetrics.get(3), Set.of(), neo4jCountryA);
            Neo4jCity neo4jCityE = Neo4jCity.of("Koh-Samui", Neo4jCityMetrics.get(4), Set.of(), neo4jCountryA);

//            Neo4jCityA = Neo4jCityA.addRoute(Neo4jCityB, 3.0, 2.5, TransportType.BUS);
//
//            Neo4jCityB = Neo4jCityB.addRoute(Neo4jCityC, 2.0, 2.4, TransportType.FLIGHT);
//            Neo4jCityB = Neo4jCityB.addRoute(Neo4jCityC, 2.0, 3.0, TransportType.BUS);
//            Neo4jCityB = Neo4jCityB.addRoute(Neo4jCityD, 1.0, 2.2, TransportType.BUS);
//
//            Neo4jCityC = Neo4jCityC.addRoute(Neo4jCityE, 1.0, 1.4, TransportType.BUS);
//            Neo4jCityC = Neo4jCityC.addRoute(Neo4jCityD, 1.0, 2.3, TransportType.BUS);
//
//            Neo4jCityD = Neo4jCityD.addRoute(Neo4jCityE, 0.0, 1.3, TransportType.BUS);
//
//            Neo4jCityE = Neo4jCityE.addRoute(Neo4jCityC, 1.0, 1.0, TransportType.BUS);

            neo4jCityService.createOrUpdateCity(neo4jCityA);
            neo4jCityService.createOrUpdateCity(neo4jCityB);

            neo4jCityService.createOrUpdateCity(neo4jCityB);
            neo4jCityService.createOrUpdateCity(neo4jCityC);
            neo4jCityService.createOrUpdateCity(neo4jCityD);
            neo4jCityService.createOrUpdateCity(neo4jCityE);

        };
    }
}