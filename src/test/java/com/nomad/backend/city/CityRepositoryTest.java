package com.nomad.backend.city;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nomad.backend.TestConfig;
import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.CountryRepository;
import com.nomad.backend.country.domain.Country;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@Slf4j
@Import(TestConfig.class)
public class CityRepositoryTest {

    private static Neo4j embeddedDatabaseServer;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    CityMetrics cityMetrics = new CityMetrics(
            new CityMetric(CityCriteria.SAILING, 8),
            new CityMetric(CityCriteria.FOOD, 5),
            new CityMetric(CityCriteria.NIGHTLIFE, 4)
    );

    String countryId = "60957a6b-4b36-40b4-83c8-bdf338458863";
    Country country =  new Country(countryId, "CountryA", "", Set.of());

    String cityAName = "CityA";
    String cityBName = "CityB";
    City cityA = City.of(cityAName, "", cityMetrics, Set.of(), country);
    City cityB = City.of(cityBName, "", cityMetrics, Set.of(), country);

    Route routeAToB = Route.of(cityB, 4, 3, TransportType.BUS);

    @BeforeAll
    static void initializeNeo4j() {
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .build();
    }

    @BeforeEach
    void setup() {
        countryRepository.save(country);
    }


    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.neo4j.uri", embeddedDatabaseServer::boltURI);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "mypassword");
    }

    @AfterAll
    static void stopNeo4j() {
        embeddedDatabaseServer.close();
    }

    boolean areCitiesEqual(City city1, City city2) {
        try {
            assertThat(city1)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "routes.id",  "routes.targetCity.id",  "routes.targetCity.routes", "routes.targetCity.country", "routes.targetCity.cityMetrics", "routes.targetCity.description")
                    .isEqualTo(city2);
            return true;
        } catch (AssertionError e) {
            return false;
        }
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
    void findById_shouldReturnCityWithoutNextHops_WhenCityExists() throws JsonProcessingException {
        City cityABeforeRoutes = cityA;
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA.mapifyCity());

        String cityAId = fetchId(cityAName);
        City createdCity = cityRepository.findById(cityAId).get();
        assertThat(areCitiesEqual(createdCity, cityABeforeRoutes)).isTrue();
    }

    @Test
    void findById_shouldReturnEmptyOptionalOfCity_WhenCityDoesntExist() {
        String cityId = "notfound";

        Optional<City> createdCity = cityRepository.findById(cityId);
        assertThat(createdCity).isEmpty();
    }

    @Test
    void findByIdFetchRoutes_shouldReturnCityWithNextHops_WhenCityExists() throws JsonProcessingException {
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA.mapifyCity());

        String cityAId = fetchId(cityAName);
        City createdCity = cityRepository.findByIdFetchRoutes(cityAId).get();

        assertThat(areCitiesEqual(createdCity, cityA)).isTrue();
    }

    @Test
    void findByIdFetchRoutes_shouldReturnEmptyOptionalOfCity_WhenCityDoesntExist() {
        String cityId = "notfound";

        Optional<City> createdCity = cityRepository.findByIdFetchRoutes(cityId);

        assertThat(createdCity).isEmpty();
    }

    @Test
    void saveCityWithDepth0_createsCityNode_IfNotExist() throws JsonProcessingException {
        List<City> allCities = cityRepository.findAll();
        cityRepository.saveCityWithDepth0(cityA.mapifyCity());

        String cityAId = fetchId(cityAName);
        City createdCity = cityRepository.findById(cityAId).get();

        assertThat(allCities).isEmpty();
        assertThat(areCitiesEqual(createdCity, cityA)).isTrue();
    }

    @Test
    void saveCityWithDepth0_doesntRecreateCityNode_IfExist() throws JsonProcessingException {

        cityRepository.saveCityWithDepth0(cityA.mapifyCity());
        String firstSaveId = fetchId(cityAName);
        City cityAFirstSave = cityRepository.findByIdFetchRoutes(firstSaveId).get();

        cityRepository.saveCityWithDepth0(cityA.mapifyCity());
        String secondSaveId = fetchId(cityAName);

        List<City> allCities = cityRepository.findAll();

        assertThat(firstSaveId).isEqualTo(secondSaveId);
        assertThat(allCities).isEqualTo(List.of(cityAFirstSave));
    }

    @Test
    void saveCityWithDepth0_overwritesCityNodeDescriptionAndMetrics_IfExist() throws JsonProcessingException {

        cityRepository.saveCityWithDepth0(cityA.mapifyCity());
        String firstSaveId = fetchId(cityAName);
        City cityAFirstSave = cityRepository.findByIdFetchRoutes(firstSaveId).get();

        String newDescription = "SomethingDifferent";
        CityMetrics newCityMetrics = new CityMetrics(
                new CityMetric(CityCriteria.SAILING, 3),
                new CityMetric(CityCriteria.FOOD, 4),
                new CityMetric(CityCriteria.NIGHTLIFE, 3)
        );
        City cityADifferent = City.of(cityAName, newDescription, newCityMetrics, Set.of(), country);

        cityRepository.saveCityWithDepth0(cityADifferent.mapifyCity());
        String cityADifferentId = fetchId(cityAName);
        City createdCityA = cityRepository.findById(cityADifferentId).get();

        assertThat(createdCityA.getCityMetrics()).isNotEqualTo(cityAFirstSave.getCityMetrics());
        assertThat(createdCityA.getDescription()).isNotEqualTo(cityAFirstSave.getDescription());
        assertThat(firstSaveId).isEqualTo(cityADifferentId);
    }

    @Test
    void saveCityWithDepth0_createsBiDirectionalRelationshipToCountry_IfNotExist() throws JsonProcessingException {

        cityRepository.saveCityWithDepth0(cityA.mapifyCity());
        String cityAId = fetchId(cityAName);
        City createdCity = cityRepository.findByIdFetchRoutes(cityAId).get();

        Country dbCountry = countryRepository.findByNameFetchCities(country.getName()).get();

        assertThat(createdCity.getCountry().getName()).isEqualTo(dbCountry.getName());
        assertThat(dbCountry.getCities().stream().findFirst().get())
                .usingRecursiveComparison()
                .ignoringFields("routes", "country")
                .isEqualTo(createdCity);
    }

    @Test
    void saveCityWithDepth0_createsTargetCityNodeAndSetsProperties_IfNotExist() throws JsonProcessingException {
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA.mapifyCity());

        String cityBId = fetchId(cityBName);

        City createdCityB = cityRepository.findById(cityBId).get();

        assertThat(createdCityB).isNotNull();
        assertThat(areCitiesEqual(createdCityB, cityB)).isTrue();
    }


}
