package com.nomad.backend.country;

import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.domain.Country;
import com.nomad.backend.exceptions.NotFoundRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryService countryService;

    String cityAName = "CityA";
    String cityBName = "CityB";

    @Mock
    CityMetrics cityMetrics;

    City cityA = City.of(cityAName, "", cityMetrics, Set.of(), null);
    City cityB = City.of(cityBName, "", cityMetrics, Set.of(), null);

    String countryName = "CountryA";
    String countryId = "1226a656-0450-4156-a522-4ae588caa937";
    Country country = new Country(countryId, countryName, "", Set.of(cityA, cityB));

    @Test
    void findAllCountries_shouldReturnAllCountries() {
        Mockito.when(countryRepository.findAllCountries()).thenReturn(Set.of(country));

        Set<Country> allCountries = countryService.findAllCountries();

        assertThat(allCountries).isEqualTo(Set.of(country));
    }

    @Test
    void getCitiesGivenCountry_shouldReturnACountriesSetOfCities_whenGivenAValidCountryId() {
        Mockito.when(countryRepository.findByIdFetchCities(countryId)).thenReturn(Optional.of(country));

        Set<City> allCities = countryService.getCitiesGivenCountry(countryId);

        assertThat(allCities).isEqualTo(Set.of(cityA, cityB));
    }

    @Test
    void getCitiesGivenCountry_shouldReturnEmptySet_whenGivenAValidIdForACountryWithNoCities() {
        Mockito.when(countryRepository.findByIdFetchCities(countryId)).thenReturn(Optional.of(new Country(country.getId(), country.getName(), country.getDescription(), Set.of())));

        Set<City> allCities = countryService.getCitiesGivenCountry(countryId);

        assertThat(allCities).isEmpty();
    }

    @Test
    void getCitiesGivenCountry_shouldThrowNotFoundRequestException_whenGivenInvalidCountryId() {

        Mockito.when(countryRepository.findByIdFetchCities("invalid")).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundRequestException.class, () -> countryService.getCitiesGivenCountry("invalid"));

    }
}
