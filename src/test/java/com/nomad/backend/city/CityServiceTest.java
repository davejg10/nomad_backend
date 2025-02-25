package com.nomad.backend.city;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    String countryId = "f1f9416f-0e7c-447c-938c-5d39cf10dad3";
    Country country = new Country(countryId, "CountryA", "", Set.of());

    String cityAId = "1226a656-0450-4156-a522-4ae588caa937";
    String cityBId = "f19c59be-a9b1-4fa4-a96c-e11f2bb111c0";

    String cityAName = "CityA";
    String cityBName = "CityB";
    @Mock
    CityMetrics cityMetrics;

    City cityA = new City(cityAId, cityAName, "", cityMetrics, Set.of(), country);
    City cityB = new City(cityBId, cityBName, "", cityMetrics, Set.of(), country);

    @Test
    void getCity_shouldReturnCity_whenCityExists() {
        City cityAWithRoute = cityA.addRoute(cityB, 3, 4, 16.0, TransportType.BUS);
        Mockito.when(cityRepository.findById(cityAId)).thenReturn(Optional.of(cityA));
        Mockito.when(cityRepository.findByIdFetchRoutes(cityAId)).thenReturn(Optional.of(cityAWithRoute));

        City returnedCity = cityService.getCity(cityAId, false);
        assertThat(returnedCity).isEqualTo(cityA);

        returnedCity = cityService.getCity(cityAId, true);

        assertThat(returnedCity).isEqualTo(cityAWithRoute);

        verify(cityRepository, times(1)).findById(cityAId);
        verify(cityRepository, times(1)).findByIdFetchRoutes(cityAId);
    }


    @Test
    void getCity_shouldThrowNotFoundRequestException_whenCityDoesntExist() {
        Mockito.when(cityRepository.findById(cityAId)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundRequestException.class, () -> cityService.getCity(cityAId, false));
        Mockito.when(cityRepository.findByIdFetchRoutes(cityAId)).thenReturn(Optional.empty());
        exception = assertThrows(NotFoundRequestException.class, () -> cityService.getCity(cityAId, true));

    }


    @Test
    void getCityFetchRoutesWithCountryId_shouldReturnCity_whenCityExists() {
        cityA = cityA.addRoute(cityB, 3, 4, 16.0, TransportType.BUS);
        Mockito.when(cityRepository.findByIdFetchRoutesByCountryId(cityAId, countryId)).thenReturn(Optional.of(cityA));

        City returnedCity = cityService.getCityFetchRoutesWithCountryId(cityAId, countryId);

        assertThat(returnedCity).isEqualTo(cityA);
    }

    @Test
    void getCityFetchRoutesWithCountryId_shouldThrowNotFoundRequestException_whenCityDoesntExist() {
        Mockito.when(cityRepository.findByIdFetchRoutesByCountryId(cityAId, countryId)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundRequestException.class, () -> cityService.getCityFetchRoutesWithCountryId(cityAId, countryId));

    }
}
