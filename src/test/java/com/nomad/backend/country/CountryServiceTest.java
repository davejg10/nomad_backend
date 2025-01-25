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
    Country country =  Country.of(countryName, "", Set.of(cityA, cityB));

    @Test
    void findAllCountries_shouldReturnAllCountries() {
        Mockito.when(countryRepository.findAllCountries()).thenReturn(Set.of(country));

        Set<Country> allCountries = countryService.findAllCountries();

        assertThat(allCountries).isEqualTo(Set.of(country));
    }

    @Test
    void getCountryByName_shouldReturnAllCountriesWithCitiesFieldPopulated_whenReturnAllCitiesIsTrue() {
        Mockito.when(countryRepository.findByNameFetchCities(countryName)).thenReturn(Optional.of(country));

        Country country = countryService.getCountryByName(countryName, true);

        assertThat(country.getCities()).isEqualTo(Set.of(cityA, cityB));
    }

    @Test
    void getCountryByName_shouldReturnAllCountriesWithoutCitiesFieldPopulated_whenReturnAllCitiesIsFalse() {
        Mockito.when(countryRepository.findByName(countryName)).thenReturn(Optional.of(Country.of(country.getName(), country.getDescription(), Set.of())));

        Country country = countryService.getCountryByName(countryName, false);

        assertThat(country.getCities()).isEmpty();
    }

    @Test
    void getCountryByName_shouldThrowNotFoundRequestException_whenCountryDoestExist() {

        Mockito.when(countryRepository.findByName(countryName)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundRequestException.class, () -> countryService.getCountryByName(countryName, false));

    }
}
