package com.nomad.backend.city;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@Slf4j
@Import(TestConfig.class)
public class CityRepositoryTest {

    private static Neo4j embeddedDatabaseServer;

    private CityRepository cityRepository;
    private CountryRepository countryRepository;

    CityMetrics cityMetrics = new CityMetrics(
            new CityMetric(CityCriteria.SAILING, 8),
            new CityMetric(CityCriteria.FOOD, 5),
            new CityMetric(CityCriteria.NIGHTLIFE, 4)
    );

    Country country =  Country.of("CountryA", "", Set.of());

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
    void setup(@Autowired Neo4jClient client, @Autowired Neo4jMappingContext schema, @Autowired ObjectMapper objectMapper) {
        cityRepository = new CityRepository(client, objectMapper, schema);
        countryRepository = new CountryRepository(client, objectMapper, schema);

        countryRepository.saveCountryWithDepth0(country);
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
                    .ignoringFields("id", "routes.id",  "routes.targetCity.id",  "routes.targetCity.routes", "routes.targetCity.country", "country.id")
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
    void findById_shouldReturnEmptyOptionalOfCity_WhenCityDoesntExist() {
        String cityId = "notfound";

        Optional<City> fetchedCity = cityRepository.findById(cityId);
        assertThat(fetchedCity).isEmpty();
    }

    @Test
    void findById_shouldReturnCity_whenCityExists() {
        City createdCity = cityRepository.saveCityWithDepth0(cityA);

        City fetchedCity = cityRepository.findById(createdCity.getId()).get();

        assertThat(areCitiesEqual(fetchedCity, cityA)).isTrue();
    }

    @Test
    void findById_shouldNotPopulateRoutesRelationship_whenCityHasRoutes() {
        City cityABeforeRoutes = cityA;
        cityA = cityA.addRoute(routeAToB);
        City createdCity = cityRepository.saveCityWithDepth0(cityA);

        City fetchedCity = cityRepository.findById(createdCity.getId()).get();
        assertThat(areCitiesEqual(fetchedCity, cityABeforeRoutes)).isTrue();
        assertThat(fetchedCity.getRoutes()).isEmpty();
    }

    @Test
    void findById_shouldPopulateCountryRelationship_always() {
        City createdCity = cityRepository.saveCityWithDepth0(cityA);

        City fetchedCity = cityRepository.findById(createdCity.getId()).get();

        assertThat(fetchedCity.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(country);
    }

    @Test
    void findByIdFetchRoutes_shouldReturnEmptyOptionalOfCity_whenCityDoesntExist() {
        String cityId = "notfound";

        Optional<City> fetchedCity = cityRepository.findByIdFetchRoutes(cityId);

        assertThat(fetchedCity).isEmpty();
    }

    @Test
    void findByIdFetchRoutes_shouldReturnCity_whenCityExists() {
        City createdCity = cityRepository.saveCityWithDepth0(cityA);

        City fetchedCity = cityRepository.findByIdFetchRoutes(createdCity.getId()).get();

        assertThat(areCitiesEqual(fetchedCity, cityA)).isTrue();
    }

    @Test
    void findByIdFetchRoutes_shouldPopulateCountryRelationship_always() {
        cityA = cityA.addRoute(routeAToB);
        City createdCityA = cityRepository.saveCityWithDepth0(cityA);
        City createdCityB = cityRepository.saveCityWithDepth0(cityB);

        City fetchedCityA = cityRepository.findByIdFetchRoutes(createdCityA.getId()).get();

        City fetchedCityB = cityRepository.findByIdFetchRoutes(createdCityB.getId()).get();

        assertThat(fetchedCityA.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(country);
        assertThat(fetchedCityB.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(country);
    }

    @Test
    void findByIdFetchRoutes_shouldPopulateRoutesRelationship_whenCityHasRoutes() {
        cityA = cityA.addRoute(routeAToB);
        City createdCity = cityRepository.saveCityWithDepth0(cityA);

        City fetchedCity = cityRepository.findByIdFetchRoutes(createdCity.getId()).get();

        assertThat(areCitiesEqual(fetchedCity, cityA)).isTrue();
        assertThat(fetchedCity.getRoutes()).isNotEmpty();
    }

    @Test
    void findByIdFetchRoutes_shouldNotPopulateRoutesRelationship_whenCityDoesntHaveAnyRoutes() {
        City createdCity = cityRepository.saveCityWithDepth0(cityA);

        City fetchedCity = cityRepository.findByIdFetchRoutes(createdCity.getId()).get();

        assertThat(areCitiesEqual(fetchedCity, cityA)).isTrue();
        assertThat(fetchedCity.getRoutes()).isEmpty();
    }

    @Test
    void findAllCities_shouldReturnEmptyList_whenNoCitiesExist() {
        Set<City> allCities = cityRepository.findAllCities();

        assertThat(allCities).isEmpty();
    }

    @Test
    void findAllCities_shouldReturnListContainingAllCities_ifCitiesExist() {
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA);
        cityRepository.saveCityWithDepth0(cityB);

        Set<City> allCities = cityRepository.findAllCities();
        List<String> allCityNames = allCities.stream().map(City::getName).toList();
        assertThat(allCities.size()).isEqualTo(2);
        assertThat(allCityNames.size()).isEqualTo(2);
        assertThat(allCityNames).containsAll(List.of("CityA", "CityB"));
    }

    @Test
    void findAllCities_shouldPopulateRoutesRelationship_ifRoutesExist() {
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA);
        cityRepository.saveCityWithDepth0(cityB);

        Set<City> allCities = cityRepository.findAllCities();

        City createdCityA = allCities.stream().filter((city -> city.getName().equals(cityAName))).findFirst().get();
        City createdCityB = allCities.stream().filter((city -> city.getName().equals(cityBName))).findFirst().get();

        assertThat(areCitiesEqual(createdCityA, cityA)).isTrue();
        assertThat(createdCityB.getRoutes()).isEmpty();
    }

    @Test
    void findAllCities_shouldPopulateCountriesRelationship_always() {
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA);
        cityRepository.saveCityWithDepth0(cityB);

        Set<City> allCities = cityRepository.findAllCities();
        assertThat(allCities.size()).isEqualTo(2);
        for (City city : allCities) {

            assertThat(city.getCountry())
                    .usingRecursiveComparison()
                    .ignoringFields("id", "cities")
                    .isEqualTo(country);
        }
    }

    @Test
    void saveCityWithDepth0_createsCityNode_ifNotExist() {
        Set<City> allCities = cityRepository.findAllCities();
        String cityAId = cityRepository.saveCityWithDepth0(cityA).getId();

        City createdCity = cityRepository.findById(cityAId).get();

        assertThat(allCities).isEmpty();
        assertThat(areCitiesEqual(createdCity, cityA)).isTrue();
    }

    @Test
    void saveCityWithDepth0_doesntRecreateCityNode_ifExist() {

        String firstSaveId = cityRepository.saveCityWithDepth0(cityA).getId();
        City cityAAfterFirstSave = cityRepository.findByIdFetchRoutes(firstSaveId).get();

        String secondSaveId = cityRepository.saveCityWithDepth0(cityA).getId();

        Set<City> allCities = cityRepository.findAllCities();

        assertThat(firstSaveId).isEqualTo(secondSaveId);
        assertThat(allCities).isEqualTo(Set.of(cityAAfterFirstSave));
    }

    @Test
    void saveCityWithDepth0_overwritesCityNodeDescriptionAndMetrics_ifExist() {

        String firstSaveId = cityRepository.saveCityWithDepth0(cityA).getId();
        City cityAAfterFirstSave = cityRepository.findByIdFetchRoutes(firstSaveId).get();

        String newDescription = "SomethingDifferent";
        CityMetrics newCityMetrics = new CityMetrics(
                new CityMetric(CityCriteria.SAILING, 3),
                new CityMetric(CityCriteria.FOOD, 4),
                new CityMetric(CityCriteria.NIGHTLIFE, 3)
        );
        cityA = City.of(cityA.getName(), newDescription, newCityMetrics, cityA.getRoutes(), cityA.getCountry());

        String cityASecondSaveId = cityRepository.saveCityWithDepth0(cityA).getId();
        City cityAAfterSecondSave = cityRepository.findById(cityASecondSaveId).get();

        assertThat(cityAAfterSecondSave.getCityMetrics()).isNotEqualTo(cityAAfterFirstSave.getCityMetrics());
        assertThat(cityAAfterSecondSave.getDescription()).isNotEqualTo(cityAAfterFirstSave.getDescription());
        assertThat(firstSaveId).isEqualTo(cityASecondSaveId);
    }

    @Test
    void saveCityWithDepth0_createsCityBiDirectionalRelationshipToCountry_ifNotExist() {

        String cityAId = cityRepository.saveCityWithDepth0(cityA).getId();
        City createdCity = cityRepository.findByIdFetchRoutes(cityAId).get();

        Country dbCountry = countryRepository.findByNameFetchCities(country.getName()).get();

        assertThat(createdCity.getCountry().getName()).isEqualTo(dbCountry.getName());
        assertThat(dbCountry.getCities().stream().findFirst().get())
                .usingRecursiveComparison()
                .ignoringFields("routes", "country")
                .isEqualTo(createdCity);
    }

    @Test
    void saveCityWithDepth0_createsTargetCityNodeAndSetsProperties_ifNotExist() {
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA);

        String cityBId = fetchId(cityBName);

        City createdCityB = cityRepository.findById(cityBId).get();

        assertThat(createdCityB).isNotNull();
        assertThat(areCitiesEqual(createdCityB, cityB)).isTrue();
        assertThat(createdCityB)
                .usingRecursiveComparison()
                .ignoringFields("id", "country")
                .isEqualTo(cityB);
    }

    @Test
    void saveCityWithDepth0_doesntOverwriteTargetCityNodeProperties_ifExist() {
        City cityBAfterFirstSave = cityRepository.saveCityWithDepth0(cityB);

        String newDescription = "SomethingDifferent";
        CityMetrics newCityMetrics = new CityMetrics(
                new CityMetric(CityCriteria.SAILING, 3),
                new CityMetric(CityCriteria.FOOD, 4),
                new CityMetric(CityCriteria.NIGHTLIFE, 3)
        );
        cityB = City.of(cityBName, newDescription, newCityMetrics, Set.of(), country);
        cityA = cityA.addRoute(Route.of(cityB, 3, 4, TransportType.BUS));

        cityRepository.saveCityWithDepth0(cityA).getId();

        String cityBId = fetchId(cityBName);
        City cityBAfterSecondSave = cityRepository.findById(cityBId).get();

        assertThat(cityBAfterFirstSave.getCityMetrics()).isEqualTo(cityBAfterSecondSave.getCityMetrics());
        assertThat(cityBAfterFirstSave.getDescription()).isEqualTo(cityBAfterSecondSave.getDescription());
        assertThat(cityBAfterFirstSave.getId()).isEqualTo(cityBAfterSecondSave.getId());
    }

    @Test
    void saveCityWithDepth0_createsTargetCityBiDirectionalRelationshipToCountry_ifNotExist() {
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA).getId();

        String cityBId = fetchId(cityBName);
        City createdCityB = cityRepository.findById(cityBId).get();
        Country dbCountry = countryRepository.findByNameFetchCities(country.getName()).get();

        assertThat(createdCityB.getCountry().getName()).isEqualTo(dbCountry.getName());

        Set<City> dbCountryCityB = dbCountry.getCities().stream().filter(city -> Objects.equals(city.getName(), cityBName)).collect(Collectors.toSet());
        assertThat(dbCountryCityB)
                .usingRecursiveComparison()
                .ignoringFields("routes", "country")
                        .isEqualTo(Set.of(createdCityB));
        assertThat(areCitiesEqual(createdCityB, cityB)).isTrue();
    }

    @Test
    void saveCityWithDepth0_doesntRecreateTargetCityNode_ifExist() {

        City cityBAfterFirstSave = cityRepository.saveCityWithDepth0(cityB);

        cityA = cityA.addRoute(routeAToB);
        City cityASave = cityRepository.saveCityWithDepth0(cityA);

        City cityBAfterSecondSave = cityRepository.findByIdFetchRoutes(cityBAfterFirstSave.getId()).get();

        Set<City> allCities = cityRepository.findAllCities();

        assertThat(cityBAfterFirstSave.getId()).isEqualTo(cityBAfterSecondSave.getId());
        assertThat(cityBAfterFirstSave).isEqualTo(cityBAfterSecondSave);
        // Order doesnt matter here
        assertThat(allCities).containsAll(List.of(cityASave, cityBAfterFirstSave));
        assertThat(List.of(cityASave, cityBAfterFirstSave)).containsAll(allCities);
    }

    @Test
    void saveCityWithDepth0_createRouteRelationshipToTargetCityNode_ifNotExist() {
        cityA = cityA.addRoute(routeAToB);
        City createdCityA = cityRepository.saveCityWithDepth0(cityA);

        Route createdRouteAToB = createdCityA.getRoutes().stream().findFirst().get();
        assertThat(createdCityA.getRoutes().size()).isEqualTo(1);
        assertThat(createdRouteAToB)
                .usingRecursiveComparison()
                .ignoringFields("id", "targetCity.country", "targetCity.id")
                .isEqualTo(routeAToB);

    }

    @Test
    void saveCityWithDepth0_recreatesRouteRelationshipToTargetCityNode_ifTransportTypeIsSameAndPopularityIsDifferent() {
        cityA = cityA.addRoute(routeAToB);
        City cityAAfterFirstSave = cityRepository.saveCityWithDepth0(cityA);

        City cityAResetRoutes = City.of(cityA.getName(), cityA.getDescription(), cityA.getCityMetrics(), Set.of(), cityA.getCountry());
        cityAResetRoutes = cityAResetRoutes.addRoute(Route.of(cityB, routeAToB.getPopularity() -1, routeAToB.getWeight(), routeAToB.getTransportType()));
        City cityAAfterSecondSave = cityRepository.saveCityWithDepth0(cityAResetRoutes);

        Route createdRouteAToBFirstSave = cityAAfterFirstSave.getRoutes().stream().findFirst().get();
        Route createdRouteAToBSecondSave = cityAAfterSecondSave.getRoutes().stream().findFirst().get();

        assertThat(cityAAfterSecondSave.getRoutes().size()).isEqualTo(1);
        assertThat(createdRouteAToBFirstSave.getId()).isNotEqualTo(createdRouteAToBSecondSave.getId());
        assertThat(createdRouteAToBSecondSave.getPopularity()).isEqualTo(createdRouteAToBFirstSave.getPopularity() - 1);
    }

    @Test
    void saveCityWithDepth0_recreatesRouteRelationshipToTargetCityNode_ifTransportTypeIsSameAndWeightIsDifferent() {
        cityA = cityA.addRoute(routeAToB);
        City cityAAfterFirstSave = cityRepository.saveCityWithDepth0(cityA);

        City cityAResetRoutes = City.of(cityA.getName(), cityA.getDescription(), cityA.getCityMetrics(), Set.of(), cityA.getCountry());
        cityAResetRoutes = cityAResetRoutes.addRoute(Route.of(cityB, routeAToB.getPopularity(), routeAToB.getWeight() + 1, routeAToB.getTransportType()));
        City cityAAfterSecondSave = cityRepository.saveCityWithDepth0(cityAResetRoutes);

        Route createdRouteAToBFirstSave = cityAAfterFirstSave.getRoutes().stream().findFirst().get();
        Route createdRouteAToBSecondSave = cityAAfterSecondSave.getRoutes().stream().findFirst().get();

        assertThat(cityAAfterSecondSave.getRoutes().size()).isEqualTo(1);
        assertThat(createdRouteAToBFirstSave.getId()).isNotEqualTo(createdRouteAToBSecondSave.getId());
        assertThat(createdRouteAToBSecondSave.getWeight()).isEqualTo(createdRouteAToBFirstSave.getWeight() + 1);
    }

    @Test
    void saveCityWithDepth0_doesntRecreateRouteRelationshipToTargetCityNode_ifTransportTypeAndWeightAndPopularityAreSame() {
        cityA = cityA.addRoute(routeAToB);
        City cityAAfterFirstSave = cityRepository.saveCityWithDepth0(cityA);

        City cityAResetRoutes = City.of(cityA.getName(), cityA.getDescription(), cityA.getCityMetrics(), Set.of(), cityA.getCountry());
        cityAResetRoutes = cityAResetRoutes.addRoute(Route.of(cityB, routeAToB.getPopularity(), routeAToB.getWeight(), routeAToB.getTransportType()));
        City cityAAfterSecondSave = cityRepository.saveCityWithDepth0(cityAResetRoutes);

        Route createdRouteAToBFirstSave = cityAAfterFirstSave.getRoutes().stream().findFirst().get();
        Route createdRouteAToBSecondSave = cityAAfterSecondSave.getRoutes().stream().findFirst().get();

        assertThat(cityAAfterSecondSave.getRoutes().size()).isEqualTo(1);
        assertThat(createdRouteAToBFirstSave).isEqualTo(createdRouteAToBSecondSave);
    }

    @Test
    void saveCityWithDepth0_createsARouteRelationshipToTargetCityForEachTransportType_ifThereAreTwoTransportTypeRoutes() {
        cityA = cityA.addRoute(cityB, 4, 3, TransportType.BUS);
        cityA = cityA.addRoute(cityB, 4, 3, TransportType.FLIGHT);
        City createdCityA = cityRepository.saveCityWithDepth0(cityA);

        List<String> allTargetCities = createdCityA.getRoutes().stream().map(route -> route.getTargetCity().getName()).distinct().toList();
        List<TransportType> allTransportTypes = createdCityA.getRoutes().stream().map(route -> route.getTransportType()).distinct().toList();

        assertThat(createdCityA.getRoutes().size()).isEqualTo(2);
        assertThat(allTargetCities.size()).isEqualTo(1);
        assertThat(allTargetCities).isEqualTo(List.of("CityB"));
        assertThat(allTransportTypes.size()).isEqualTo(2);
        assertThat(allTransportTypes).containsAll(List.of(TransportType.BUS, TransportType.FLIGHT));
    }

    @Test
    void saveCityWithDepth0_doesntTouchTargetCityRouteRelationships_ever() {
        String cityCName = "CityC";
        City cityC = City.of(cityCName, "", cityMetrics, Set.of(), country);
        cityB = cityB.addRoute(cityC, 4, 3, TransportType.BUS);

        City cityBAfterFirstSave = cityRepository.saveCityWithDepth0(cityB);
        String cityCId = fetchId(cityCName);
        City cityCAfterFirstSave = cityRepository.findByIdFetchRoutes(cityCId).get();

        City cityBResetRoutes = City.of(cityBName, cityB.getDescription(), cityB.getCityMetrics(), Set.of(), cityB.getCountry());
        cityA = cityA.addRoute(cityBResetRoutes, 4, 3, TransportType.BUS);
        City cityAAfterSecondSave = cityRepository.saveCityWithDepth0(cityA);

        String cityBId = fetchId(cityBName);
        City cityBAfterSecondSave = cityRepository.findByIdFetchRoutes(cityBId).get();
        
        Set<City> allCities = cityRepository.findAllCities();

        assertThat(allCities.size()).isEqualTo(3);
        assertThat(allCities).containsAll(Set.of(cityBAfterFirstSave, cityCAfterFirstSave, cityAAfterSecondSave));
        assertThat(Set.of(cityBAfterFirstSave, cityCAfterFirstSave, cityAAfterSecondSave)).containsAll(allCities);

        assertThat(cityBAfterSecondSave.getRoutes()).isEqualTo(cityBAfterFirstSave.getRoutes());
        assertThat(cityBResetRoutes.getRoutes()).isNotEqualTo(cityB.getRoutes());
    }

    @Test
    void mapifyCity_shouldRecursivelyStringifyCityMetricsFields() {
        CityMetrics cityAMetrics = new CityMetrics(
                new CityMetric(CityCriteria.SAILING, 8),
                new CityMetric(CityCriteria.FOOD, 5),
                new CityMetric(CityCriteria.NIGHTLIFE, 4)
        );
        CityMetrics cityBMetrics = new CityMetrics(
                new CityMetric(CityCriteria.SAILING, 4),
                new CityMetric(CityCriteria.FOOD, 3),
                new CityMetric(CityCriteria.NIGHTLIFE, 2)
        );
        City cityA =  City.of("CityA", "", cityAMetrics, Set.of(), country);
        City cityB =  City.of("CityB", "", cityBMetrics, Set.of(), country);

        cityA = cityA.addRoute(cityB, 4, 3, TransportType.BUS);
        Map<String, Object> mapifiedCity = cityRepository.mapifyCity(cityA);

        Object cityACityMetrics = mapifiedCity.get("cityMetrics");

        ArrayList<LinkedHashMap<String, Object>> cityARoutes = (ArrayList<LinkedHashMap<String, Object>>) mapifiedCity.get("routes");
        LinkedHashMap<String, Object> routeToB = cityARoutes.get(0);
        LinkedHashMap<String, Object> cityBFetched = (LinkedHashMap<String, Object>) routeToB.get("targetCity");
        Object cityBCityMetrics = cityBFetched.get("cityMetrics");

        assertThat(cityACityMetrics).isEqualTo("{\"sailing\":{\"criteria\":\"SAILING\",\"metric\":8},\"food\":{\"criteria\":\"FOOD\",\"metric\":5},\"nightlife\":{\"criteria\":\"NIGHTLIFE\",\"metric\":4}}");
        assertThat(cityBCityMetrics).isEqualTo("{\"sailing\":{\"criteria\":\"SAILING\",\"metric\":4},\"food\":{\"criteria\":\"FOOD\",\"metric\":3},\"nightlife\":{\"criteria\":\"NIGHTLIFE\",\"metric\":2}}");
    }

}
