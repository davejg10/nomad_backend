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
import org.springframework.data.neo4j.types.GeographicPoint3d;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Configuration
@Log4j2
class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(Neo4jCityService neo4jCityService, Neo4jCityRepository neo4jCityRepository, Neo4jCountryRepository neo4jCountryRepository) {

        return args -> {
            Neo4jCountry neo4jCountryA = new Neo4jCountry(UUID.randomUUID().toString(), "Thailand", "short desc", "blob:url", Set.of());

            neo4jCountryRepository.createCountry(neo4jCountryA);

            Random random = new Random();

            List<Set<CityMetric>> Neo4jCityMetrics = IntStream.range(0, 5).boxed()
                    .map(i  -> Set.of(
                            new CityMetric(CityCriteria.SAILING, random.nextDouble(11)),
                            new CityMetric(CityCriteria.FOOD, random.nextDouble(11)),
                            new CityMetric(CityCriteria.NIGHTLIFE, random.nextDouble(11))
            )).toList();

            GeographicPoint3d coord = new GeographicPoint3d(13.7563, 100.5018, 0.0);


            Neo4jCity neo4jCityA = new Neo4jCity(UUID.randomUUID().toString(), "Bangkok", "shortdesc", "blob:url", coord, Neo4jCityMetrics.get(0), Set.of(), neo4jCountryA);
            Neo4jCity neo4jCityB = new Neo4jCity(UUID.randomUUID().toString(), "Pai", "shortdesc", "blob:url", coord,  Neo4jCityMetrics.get(1), Set.of(), neo4jCountryA);
            Neo4jCity neo4jCityC = new Neo4jCity(UUID.randomUUID().toString(), "Chiang-Mai", "shortdesc", "blob:url", coord,  Neo4jCityMetrics.get(2), Set.of(), neo4jCountryA);
            Neo4jCity neo4jCityD = new Neo4jCity(UUID.randomUUID().toString(), "Phuket", "shortdesc", "blob:url", coord,  Neo4jCityMetrics.get(3), Set.of(), neo4jCountryA);
            Neo4jCity neo4jCityE = new Neo4jCity(UUID.randomUUID().toString(), "Koh-Samui", "shortdesc", "blob:url", coord,  Neo4jCityMetrics.get(4), Set.of(), neo4jCountryA);
            neo4jCityRepository.createCity(neo4jCityA);
            neo4jCityRepository.createCity(neo4jCityB);
            neo4jCityRepository.createCity(neo4jCityC);
            neo4jCityRepository.createCity(neo4jCityD);
            neo4jCityRepository.createCity(neo4jCityE);


            neo4jCityA = neo4jCityA.addRoute(UUID.randomUUID().toString(), neo4jCityB, 3.0, 2.5, 10.0, TransportType.BUS);

            neo4jCityB = neo4jCityB.addRoute(UUID.randomUUID().toString(), neo4jCityC, 2.0, 2.4, 10.0,TransportType.FLIGHT);
            neo4jCityB = neo4jCityB.addRoute(UUID.randomUUID().toString(), neo4jCityC, 2.0, 3.0,  10.0,TransportType.BUS);
            neo4jCityB = neo4jCityB.addRoute(UUID.randomUUID().toString(), neo4jCityD, 1.0, 2.2, 10.0, TransportType.BUS);

            neo4jCityC = neo4jCityC.addRoute(UUID.randomUUID().toString(), neo4jCityE, 1.0, 1.4, 10.0, TransportType.BUS);
            neo4jCityC = neo4jCityC.addRoute(UUID.randomUUID().toString(), neo4jCityD, 1.0, 2.3, 10.0, TransportType.BUS);

            neo4jCityD = neo4jCityD.addRoute(UUID.randomUUID().toString(), neo4jCityE, 0.0, 1.3, 10.0, TransportType.BUS);

            neo4jCityE = neo4jCityE.addRoute(UUID.randomUUID().toString(), neo4jCityC, 1.0, 1.0, 10.0, TransportType.BUS);

            neo4jCityRepository.saveRoute(neo4jCityA);
            neo4jCityRepository.saveRoute(neo4jCityB);

            neo4jCityRepository.saveRoute(neo4jCityC);

            neo4jCityRepository.saveRoute(neo4jCityD);
            neo4jCityRepository.saveRoute(neo4jCityE);

//            neo4jCityService.createOrUpdateCity(neo4jCityA);
//            neo4jCityService.createOrUpdateCity(neo4jCityB);
//
//            neo4jCityService.createOrUpdateCity(neo4jCityB);
//            neo4jCityService.createOrUpdateCity(neo4jCityC);
//            neo4jCityService.createOrUpdateCity(neo4jCityD);
//            neo4jCityService.createOrUpdateCity(neo4jCityE);

        };
    }
}