package com.nomad.backend.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.TestConfig;
import com.nomad.backend.city.neo4j.Neo4jCityMappers;
import com.nomad.backend.city.neo4j.Neo4jCityRepository;
import com.nomad.backend.country.neo4j.Neo4jCountryRepository;
import com.nomad.backend.domain.RouteInfoDTO;
import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.config.Neo4jConfig;
import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.Neo4jCommonCountryMappers;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@DataNeo4jTest
@Import({TestConfig.class, Neo4jTestConfiguration.class, Neo4jConfig.class})
@Transactional
public class Neo4jCityRepositoryTest {

    private Neo4jCityRepository cityRepository;
    private Neo4jCountryRepository countryRepository;

    Neo4jCountry countryA =  Neo4jTestGenerator.neo4jCountryNoCities("CountryA");
    Neo4jCountry countryB =  Neo4jTestGenerator.neo4jCountryNoCities("CountryB");

    Neo4jCountry savedCountryA;
    Neo4jCountry savedCountryB;

    String cityAName = "CityA";
    String cityBName = "CityB";
    Neo4jCity cityA = Neo4jTestGenerator.neo4jCityNoRoutes(cityAName, countryA);
    Neo4jCity cityB = Neo4jTestGenerator.neo4jCityNoRoutes(cityBName, countryA);

    Neo4jRoute routeAToB = Neo4jTestGenerator.neo4jRoute(cityB);

    @BeforeEach
    void setup(@Autowired Neo4jClient client, @Autowired Neo4jMappingContext schema, @Autowired ObjectMapper objectMapper) throws Neo4jGenericException {
        Neo4jCityMappers neo4jCityMappers = new Neo4jCityMappers(schema);
        Neo4jCommonCountryMappers neo4jCountryMappers = new Neo4jCommonCountryMappers(schema);
        
        cityRepository = new Neo4jCityRepository(client, objectMapper, neo4jCityMappers);
        countryRepository = new Neo4jCountryRepository(client, objectMapper, neo4jCountryMappers);

        savedCountryA = countryRepository.createCountry(countryA);
        savedCountryB = countryRepository.createCountry(countryB);
    }

    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldReturnEmptyOptionalOfCity_whenCityDoesntExist() {
        String cityId = "notfound";

        Optional<Neo4jCity> fetchedCity = cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityId, savedCountryA.getId());

        assertThat(fetchedCity).isEmpty();
    }

    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldReturnCity_whenCityExists() {
        cityRepository.createCity(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityA.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCity).isEqualTo(cityA);
    }

    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldPopulateCountryRelationship_always() {
        cityRepository.createCity(cityA);
        cityRepository.createCity(cityB);

        Neo4jCity fetchedCityA = cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityA.getId(), savedCountryA.getId()).get();

        Neo4jCity fetchedCityB = cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityB.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCityA.getCountry()).isEqualTo(countryA);
        assertThat(fetchedCityB.getCountry()).isEqualTo(countryA);
    }

    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldPopulateTargetCitiesCountryRelationship_always() {
        cityRepository.createCity(cityA);
        cityRepository.createCity(cityB);
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveRoute(cityA);

        Neo4jCity fetchedCityA = cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityA.getId(), savedCountryA.getId()).get();

        Neo4jCity fetchedCityB = fetchedCityA.getRoutes().stream().findFirst().get().getTargetCity();

        assertThat(fetchedCityB.getCountry()).isEqualTo(countryA);
    }

    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldPopulateRoutesRelationship_onlyWithTargetCitiesLinkedToCountryId() {
        Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes("CityC", countryB);
        cityRepository.createCity(cityA);
        cityRepository.createCity(cityB);
        cityRepository.createCity(cityC);
        cityA = cityA.addRoute(routeAToB);
        cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityC));
        cityRepository.saveRoute(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityA.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCity.getRoutes().size()).isEqualTo(1);
        assertThat(cityA.getRoutes().size()).isEqualTo(2);
        assertThat(fetchedCity.getRoutes().stream().findFirst().get().getTargetCity().getName()).isEqualTo(cityBName);
    }

    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldNotPopulateRoutesRelationship_whenCityDoesntHaveAnyRoutes() {
        cityRepository.createCity(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityA.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCity).isEqualTo(cityA);
        assertThat(fetchedCity.getRoutes()).isEmpty();
    }

    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldNotPopulateRoutesRelationship_whenCityHasRoutesButNoneWithTargetCitiesLinkedToCountryId() {
        Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes("CityC", countryB);
        cityRepository.createCity(cityA);
        cityRepository.createCity(cityC);
        cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityC));
        cityRepository.saveRoute(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityA.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCity.getRoutes().size()).isEqualTo(0);
        assertThat(cityA.getRoutes().size()).isEqualTo(1);
    }
    
     @Test
     void findByIdAndCountryIdOrderByPreferences_shouldReturnCityWithRoutesInOrder() {
         Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes("CityC", countryA);
         Neo4jCity cityD = Neo4jTestGenerator.neo4jCityNoRoutes("CityD", countryA);
         Neo4jCity cityE = Neo4jTestGenerator.neo4jCityNoRoutes("CityE", countryB);

         Neo4jCity createdCityA = cityRepository.createCity(cityA);
         cityRepository.createCity(cityB);
         cityRepository.createCity(cityC);
         cityRepository.createCity(cityD);
         cityRepository.createCity(cityE);

         Neo4jRoute toB = Neo4jTestGenerator.neo4jRoute(cityB);
         Neo4jRoute toB2 = Neo4jTestGenerator.neo4jRoute(cityB);
         Neo4jRoute toB3 = Neo4jTestGenerator.neo4jRoute(cityB);

         Neo4jRoute toC = Neo4jTestGenerator.neo4jRoute(cityC);
         Neo4jRoute toD = Neo4jTestGenerator.neo4jRoute(cityD);
         Neo4jRoute toE = Neo4jTestGenerator.neo4jRoute(cityE);
         createdCityA = createdCityA.addRoute(toB).addRoute(toC).addRoute(toD).addRoute(toE).addRoute(toB2).addRoute(toB3);
         cityRepository.saveRoute(createdCityA);

         Optional<Neo4jCity> secondSave = cityRepository.findByIdFetchRoutes(createdCityA.getId());
         log.info(secondSave);

         Map<String, String> cityCriteriaPreferences = Map.of(
                 CityCriteria.FOOD.name(), "3",
                 CityCriteria.NIGHTLIFE.name(), "3",
                 CityCriteria.SAILING.name(), "3"
         );
         int costPreference = 2;
         Set<RouteInfoDTO> allRoutesOrdered = cityRepository.fetchRoutesByTargetCityCountryIdsOrderByPreferences(createdCityA.getId(), Set.of(countryA.getId(), countryB.getId()), cityCriteriaPreferences, costPreference);
         log.info(allRoutesOrdered);


     }

    

}
