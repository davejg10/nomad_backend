package com.nomad.backend.city;


import com.nomad.backend.city.neo4j.Neo4jCityRepository;
import com.nomad.backend.city.neo4j.Neo4jCityService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class Neo4jCityServiceTest {

    @Mock
    private Neo4jCityRepository cityRepository;

    @InjectMocks
    private Neo4jCityService cityService;

    String targetCityCountryId = "f1f9416f-0e7c-447c-938c-5d39cf10dad3";
    Neo4jCountry country = Neo4jTestGenerator.neo4jCountryNoCities("CountryA").withId(targetCityCountryId);
    
    String cityAId = UUID.randomUUID().toString();
    String cityBId = UUID.randomUUID().toString();

    String cityAName = "CityA";
    String cityBName = "CityB";
    
    Neo4jCity cityA = Neo4jTestGenerator.neo4jCityNoRoutesWithId(cityAId, "CityA", country);
    Neo4jCity cityB =  Neo4jTestGenerator.neo4jCityNoRoutesWithId(cityBId, "CityB", country);

    @Test
    void findById_shouldReturnCity_whenCityExists() {
        Neo4jCity cityAWithRoute = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityB));
        Mockito.when(cityRepository.findById(cityAId)).thenReturn(Optional.of(cityA));
        Mockito.when(cityRepository.findByIdFetchRoutes(cityAId)).thenReturn(Optional.of(cityAWithRoute));

        Neo4jCity returnedCity = cityService.findById(cityAId, false);
        assertThat(returnedCity).isEqualTo(cityA);

        returnedCity = cityService.findById(cityAId, true);

        assertThat(returnedCity).isEqualTo(cityAWithRoute);

        verify(cityRepository, times(1)).findById(cityAId);
        verify(cityRepository, times(1)).findByIdFetchRoutes(cityAId);
    }


    @Test
    void findById_shouldThrowNotFoundRequestException_whenCityDoesntExist() {
        Mockito.when(cityRepository.findById(cityAId)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundRequestException.class, () -> cityService.findById(cityAId, false));
        Mockito.when(cityRepository.findByIdFetchRoutes(cityAId)).thenReturn(Optional.empty());
        exception = assertThrows(NotFoundRequestException.class, () -> cityService.findById(cityAId, true));
    }


    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldReturnCity_whenCityExists() {
        cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityB));
        Mockito.when(cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityAId, targetCityCountryId)).thenReturn(Optional.of(cityA));

        Neo4jCity returnedCity = cityService.findByIdFetchRoutesByTargetCityCountryId(cityAId, targetCityCountryId);

        assertThat(returnedCity).isEqualTo(cityA);
    }

    @Test
    void findByIdFetchRoutesByTargetCityCountryId_shouldThrowNotFoundRequestException_whenCityDoesntExist() {
        Mockito.when(cityRepository.findByIdFetchRoutesByTargetCityCountryId(cityAId, targetCityCountryId)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundRequestException.class, () -> cityService.findByIdFetchRoutesByTargetCityCountryId(cityAId, targetCityCountryId));

    }
}
