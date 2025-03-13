package com.nomad.backend.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.TestConfig;
import com.nomad.backend.country.Neo4jCountryRepository;
import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.config.Neo4jConfig;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.exceptions.Neo4jGenericException;

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
        cityRepository = new Neo4jCityRepository(client, objectMapper, schema);
        countryRepository = new Neo4jCountryRepository(client, objectMapper, schema);

        savedCountryA = countryRepository.createCountry(countryA);
        savedCountryB = countryRepository.createCountry(countryB);
    }

    @Autowired
    Neo4jClient client;

    String fetchId(String cityName) {
        Map<String, Object> cityId = client
                .query(
                        "MATCH (city:City {name: $cityName}) " +
                                "RETURN city.id as id"
                )
                .bind(cityName).to("cityName")
                .fetch()
                .first()
                .get();
        return cityId.get("id").toString();
    }

    @Test
    void findByIdFetchRoutesByCountryId_shouldReturnEmptyOptionalOfCity_whenCityDoesntExist() {
        String cityId = "notfound";

        Optional<Neo4jCity> fetchedCity = cityRepository.findByIdFetchRoutesByCountryId(cityId, savedCountryA.getId());

        assertThat(fetchedCity).isEmpty();
    }

    @Test
    void findByIdFetchRoutesByCountryId_shouldReturnCity_whenCityExists() {
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutesByCountryId(createdCity.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCity).isEqualTo(cityA);
    }

    @Test
    void findByIdFetchRoutesByCountryId_shouldPopulateCountryRelationship_always() {
        cityA = cityA.addRoute(routeAToB);
        Neo4jCity createdCityA = cityRepository.saveCityWithDepth0(cityA);
        Neo4jCity createdCityB = cityRepository.saveCityWithDepth0(cityB);

        Neo4jCity fetchedCityA = cityRepository.findByIdFetchRoutesByCountryId(createdCityA.getId(), savedCountryA.getId()).get();

        Neo4jCity fetchedCityB = cityRepository.findByIdFetchRoutesByCountryId(createdCityB.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCityA.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(countryA);
        assertThat(fetchedCityB.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(countryA);
    }

    @Test
    void findByIdFetchRoutesByCountryId_shouldPopulateTargetCitiesCountryRelationship_always() {
        cityA = cityA.addRoute(routeAToB);
        Neo4jCity createdCityA = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCityA = cityRepository.findByIdFetchRoutesByCountryId(createdCityA.getId(), savedCountryA.getId()).get();

        Neo4jCity fetchedCityB = fetchedCityA.getRoutes().stream().findFirst().get().getTargetCity();

        assertThat(fetchedCityB.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(countryA);
    }

    @Test
    void findByIdFetchRoutesByCountryId_shouldPopulateRoutesRelationship_onlyWithTargetCitiesLinkedToCountryId() {
        Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes("CityC", countryB);
        cityA = cityA.addRoute(routeAToB);
        cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityC));
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutesByCountryId(createdCity.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCity.getRoutes().size()).isEqualTo(1);
        assertThat(cityA.getRoutes().size()).isEqualTo(2);
        assertThat(fetchedCity.getRoutes().stream().findFirst().get().getTargetCity().getName()).isEqualTo(cityBName);
    }

    @Test
    void findByIdFetchRoutesByCountryId_shouldNotPopulateRoutesRelationship_whenCityDoesntHaveAnyRoutes() {
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutesByCountryId(createdCity.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCity).isEqualTo(cityA);
        assertThat(fetchedCity.getRoutes()).isEmpty();
    }

    @Test
    void findByIdFetchRoutesByCountryId_shouldNotPopulateRoutesRelationship_whenCityHasRoutesButNoneWithTargetCitiesLinkedToCountryId() {
        Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes("CityC", countryB);
        cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityC));
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutesByCountryId(createdCity.getId(), savedCountryA.getId()).get();

        assertThat(fetchedCity.getRoutes().size()).isEqualTo(0);
        assertThat(cityA.getRoutes().size()).isEqualTo(1);
    }

    

}
