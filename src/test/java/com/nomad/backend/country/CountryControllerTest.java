package com.nomad.backend.country;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.city.CityController;
import com.nomad.backend.city.CityService;
import com.nomad.backend.city.domain.City;
import com.nomad.backend.city.domain.CityCriteria;
import com.nomad.backend.city.domain.CityMetric;
import com.nomad.backend.city.domain.CityMetrics;
import com.nomad.backend.country.domain.Country;
import com.nomad.backend.exceptions.NotFoundRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = CountryController.class)
public class CountryControllerTest {

    @MockitoBean
    private CountryService countryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    String countryAName = "CountryA";
    String countryBName = "CountryB";
    String countryAId = "1226a656-0450-4156-a522-4ae588caa937";

    @Mock
    CityMetrics cityMetrics;

    City cityA = City.of("CityA", "", cityMetrics, Set.of(), null);
    City cityB = City.of("CityB", "", cityMetrics, Set.of(), null);

    Country countryA = new Country(countryAId, countryAName, "", Set.of(cityA, cityB));
    Country countryB = Country.of(countryBName, "", Set.of());


    @Test
    void getAll_shouldReturn200WrappedAllCountries_whenCountriesExist() throws Exception {

        Mockito.when(countryService.findAllCountries()).thenReturn(Set.of(countryA, countryB));

        mockMvc.perform(get("/countries"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(Set.of(countryA, countryB))))
                .andReturn();
    }

    @Test
    void getAll_shouldReturn200_whenNoCountriesExist() throws Exception {

        Mockito.when(countryService.findAllCountries()).thenReturn(Set.of());

        mockMvc.perform(get("/countries"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(Set.of())))
                .andReturn();
    }

    @Test
    void getCitiesGivenCountry_shouldReturn404_whenCountryDoesntExist() throws Exception {

        Mockito.when(countryService.getCitiesGivenCountry("not exist")).thenThrow(new NotFoundRequestException("Country not found"));

        mockMvc.perform(get(String.format("/countries/%s/cities", "not exist")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Country not found"))
                .andReturn();
    }

    @Test
    void getCitiesGivenCountry_shouldReturn200_whenCountryExists() throws Exception {
        Mockito.when(countryService.getCitiesGivenCountry(countryAId)).thenReturn(Set.of(cityA, cityB));

        mockMvc.perform(get(String.format("/countries/%s/cities", countryAId)))
                .andExpect(status().isOk())
                .andReturn();

        Mockito.when(countryService.getCitiesGivenCountry(countryAId)).thenReturn(Set.of(cityA, cityB));

        mockMvc.perform(get(String.format("/countries/%s/cities", countryAId)))
                .andExpect(status().isOk())
                .andReturn();
    }
}