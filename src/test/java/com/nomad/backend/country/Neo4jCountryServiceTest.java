package com.nomad.backend.country;

import com.nomad.backend.country.neo4j.Neo4jCountryRepository;
import com.nomad.backend.country.neo4j.Neo4jCountryService;
import com.nomad.backend.exceptions.NotFoundRequestException;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class Neo4jCountryServiceTest {

    @Mock
    private Neo4jCountryRepository countryRepository;

    @InjectMocks
    private Neo4jCountryService countryService;

    String cityAName = "CityA";
    String cityBName = "CityB";

    Neo4jCity cityA = Neo4jTestGenerator.neo4jCityNoRoutes(cityAName, null);
    Neo4jCity cityB = Neo4jTestGenerator.neo4jCityNoRoutes(cityBName, null);

    String countryName = "CountryA";
    String countryId = UUID.randomUUID().toString();
    Neo4jCountry country = new Neo4jCountry(countryId, countryName, "short desc", "blob:url", Set.of(cityA, cityB));
    
    @Test
    void findAllCountries_shouldReturnAllCountries() {
        Mockito.when(countryRepository.findAllCountries()).thenReturn(Set.of(country));

        Set<Neo4jCountry> allCountries = countryService.findAllCountries();

        assertThat(allCountries).isEqualTo(Set.of(country));
    }

    @Test
    void getCitiesGivenCountry_shouldReturnACountriesSetOfCities_whenGivenAValidCountryId() {
        Mockito.when(countryRepository.findByIdFetchCities(countryId)).thenReturn(Optional.of(country));

        Set<Neo4jCity> allCities = countryService.getCitiesGivenCountry(countryId);

        assertThat(allCities).isEqualTo(Set.of(cityA, cityB));
    }

    @Test
    void getCitiesGivenCountry_shouldReturnEmptySet_whenGivenAValidIdForACountryWithNoCities() {
        Mockito.when(countryRepository.findByIdFetchCities(countryId)).thenReturn(Optional.of(Neo4jTestGenerator.neo4jCountryNoCities(countryName).withId(countryId)));

        Set<Neo4jCity> allCities = countryService.getCitiesGivenCountry(countryId);

        assertThat(allCities).isEmpty();
    }

    @Test
    void getCitiesGivenCountry_shouldThrowNotFoundRequestException_whenGivenInvalidCountryId() {

        Mockito.when(countryRepository.findByIdFetchCities("invalid")).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundRequestException.class, () -> countryService.getCitiesGivenCountry("invalid"));

    }
}
