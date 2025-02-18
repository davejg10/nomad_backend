package com.nomad.backend.country;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.TestConfig;
import com.nomad.backend.city.CityRepository;
import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.domain.Country;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@Log4j2
@Import(TestConfig.class)
public class CountryRepositoryTest {

    private static Neo4j embeddedDatabaseServer;

    private CountryRepository countryRepository;
    private CityRepository cityRepository;

    @Mock
    CityMetrics cityMetrics;

    String countryAName = "CountryA";
    String countryBName = "CountryB";
    Country countryA =  Country.of( "CountryA", "", Set.of());
    Country countryB =  Country.of( "CountryB", "", Set.of());

    String cityAName = "CityA";
    String cityBName = "CityB";
    City cityA = City.of(cityAName, "", cityMetrics, Set.of(), countryA);
    City cityB = City.of(cityBName, "", cityMetrics, Set.of(), countryA);

    Route routeAToB = Route.of(cityB, 4, 3, 16.0, TransportType.BUS);

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

    boolean areCountriesEqual(Country country1, Country country2) {
        try {
            assertThat(country1)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(country2);
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    @Test
    void findAllCountries_shouldReturnEmptySet_whenNoCountriesExist() {
        Set<Country> allCountries = countryRepository.findAllCountries();

        assertThat(allCountries.size()).isEqualTo(0);
    }

    @Test
    void findAllCountries_shouldReturnSetContainingAllCountries_ifCountriesExist() {
        countryRepository.saveCountryWithDepth0(countryA);
        countryRepository.saveCountryWithDepth0(countryB);
        Set<Country> allCountries = countryRepository.findAllCountries();

        List<String> allCountryNames = allCountries.stream().map(Country::getName).toList();

        assertThat(allCountries.size()).isEqualTo(2);
        assertThat(allCountryNames.size()).isEqualTo(2);
        assertThat(allCountryNames).containsAll(List.of(countryAName, countryBName));
    }

    @Test
    void findAllCountries_shouldNotPopulateCitiesRelationship_ifCountryHasCities() {
        countryRepository.saveCountryWithDepth0(countryA);
        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA);

        Set<Country> allCountries = countryRepository.findAllCountries();

        assertThat(allCountries.size()).isEqualTo(1);
        assertThat(allCountries.stream().findFirst().get().getCities()).isEmpty();
    }

    @Test
    void findByIdFetchCities_shouldReturnEmptyOptional_ifCountryDoesntExist() {
        Optional<Country> createdCountry = countryRepository.findByIdFetchCities("invalid");

        assertThat(createdCountry).isEmpty();
    }

    @Test
    void findByIdFetchCities_shouldReturnCountry_ifCountryExists() {
        Country createdCountry = countryRepository.saveCountryWithDepth0(countryA);

        createdCountry = countryRepository.findByIdFetchCities(createdCountry.getId()).get();

        assertThat(areCountriesEqual(createdCountry, countryA)).isTrue();
    }

    @Test
    void findByIdFetchCities_shouldPopulateCitiesRelationship_ifCountryHasCities() {
        Country createdCountry = countryRepository.saveCountryWithDepth0(countryA);
        cityRepository.saveCityWithDepth0(cityA);

        createdCountry = countryRepository.findByIdFetchCities(createdCountry.getId()).get();

        assertThat(createdCountry.getCities()).isNotEmpty();
        assertThat(createdCountry.getCities().stream().findFirst().get())
                .usingRecursiveComparison()
                .ignoringFields("id", "routes", "country.id")
                .isEqualTo(cityA);
    }

    @Test
    void findByIdFetchCities_shouldNotPopulateCitiesRelationship_ifCountryDoesntHaveCities() {
        Country createdCountry = countryRepository.saveCountryWithDepth0(countryA);

        createdCountry = countryRepository.findByIdFetchCities(createdCountry.getId()).get();

        assertThat(areCountriesEqual(createdCountry, countryA)).isTrue();
        assertThat(createdCountry.getCities()).isEmpty();
    }


    @Test
    void saveCountryWithDepth0_createsCountryNode_ifNotExist() {
        Set<Country> allCountries = countryRepository.findAllCountries();
        Country createdCountry = countryRepository.saveCountryWithDepth0(countryA);


        assertThat(areCountriesEqual(createdCountry, countryA)).isTrue();
        assertThat(allCountries).isEmpty();
    }

    @Test
    void saveCountryWithDepth0_doesntRecreateCountry_ifExist() {
        Country countryAFirstSave = countryRepository.saveCountryWithDepth0(countryA);

        Country countryASecondSave = countryRepository.saveCountryWithDepth0(countryA);

        Set<Country> allCountries = countryRepository.findAllCountries();

        assertThat(countryAFirstSave.getId()).isEqualTo(countryASecondSave.getId());
        assertThat(allCountries).isEqualTo(Set.of(countryAFirstSave));
    }

    @Test
    void saveCountryWithDepth0_overwritesCountryDescription_ifExist() {
        Country countryAFirstSave = countryRepository.saveCountryWithDepth0(countryA);

        Country countryADifferentDescription = Country.of(countryA.getName(), "new description", countryA.getCities());
        Country countryASecondSave = countryRepository.saveCountryWithDepth0(countryADifferentDescription);

        assertThat(countryAFirstSave.getId()).isEqualTo(countryASecondSave.getId());
        assertThat(countryAFirstSave.getDescription()).isNotEqualTo(countryASecondSave.getDescription());
    }

    @Test
    void saveCountryWithDepth0_doesntTouchCityNodes_ever() {
        countryRepository.saveCountryWithDepth0(countryA);

        cityA = cityA.addRoute(routeAToB);
        cityRepository.saveCityWithDepth0(cityA);

        Set<City> allCitiesFirstSearch = cityRepository.findAllCities();

        CityMetrics newCityMetrics = new CityMetrics(
                new CityMetric(CityCriteria.SAILING, 8.0),
                new CityMetric(CityCriteria.FOOD, 5.4),
                new CityMetric(CityCriteria.NIGHTLIFE, 4.3)
        );
        City cityADifferentProperties = City.of(cityA.getName(), "different description", newCityMetrics, Set.of(), countryA);
        City cityBDifferentProperties = City.of(cityB.getName(), "another different description", newCityMetrics, Set.of(Route.of(cityADifferentProperties, 4, 3, 16.0, TransportType.BUS)), countryA);

        Country countryAWithCities = Country.of(countryA.getName(), countryA.getDescription(), Set.of(cityADifferentProperties, cityBDifferentProperties));
        countryRepository.saveCountryWithDepth0(countryAWithCities);

        Set<City> allCitiesSecondSearch = cityRepository.findAllCities();

        assertThat(allCitiesSecondSearch.size()).isEqualTo(2);
        assertThat(allCitiesFirstSearch).isEqualTo(allCitiesSecondSearch);

    }
}
