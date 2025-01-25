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

@ExtendWith(MockitoExtension.class)
public class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    Country country = Country.of("CountryA", "", Set.of());

    String cityAId = "1226a656-0450-4156-a522-4ae588caa937";
    String cityBId = "f19c59be-a9b1-4fa4-a96c-e11f2bb111c0";

    String cityAName = "CityA";
    String cityBName = "CityB";
    @Mock
    CityMetrics cityMetrics;

    City cityA = new City(cityAId, cityAName, "", cityMetrics, Set.of(), country);
    City cityB = new City(cityBId, cityBName, "", cityMetrics, Set.of(), country);

    @Test
    void getCity_shouldReturnCityWithoutRoutesFieldPopulated_whenIncludeRoutesIsFalse() {
        City cityABeforeRoutes = cityA;
        cityA.addRoute(cityB, 3, 4, TransportType.BUS);
        Mockito.when(cityRepository.findById(cityAId)).thenReturn(Optional.of(cityA));

        City returnedCity = cityService.getCity(cityAId, false);

        assertThat(returnedCity).isEqualTo(cityABeforeRoutes);
    }

    @Test
    void getCity_shouldReturnCityWithRoutesFieldPopulated_whenIncludeRoutesIsTrue() {
        cityA.addRoute(cityB, 3, 4, TransportType.BUS);
        Mockito.when(cityRepository.findByIdFetchRoutes(cityAId)).thenReturn(Optional.of(cityA));

        City returnedCity = cityService.getCity(cityAId, true);

        assertThat(returnedCity).isEqualTo(cityA);
    }

    @Test
    void getCity_shouldThrowNotFoundRequestException_whenCityDoesntExist() {
        Mockito.when(cityRepository.findById(cityAId)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundRequestException.class, () -> cityService.getCity(cityAId, false));

    }
}
