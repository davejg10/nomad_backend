package com.nomad.backend.country;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.exceptions.NotFoundRequestException;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.CityMetrics;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = Neo4jCountryController.class)
public class Neo4jCountryControllerTest {

    @MockitoBean
    private Neo4jCountryService countryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    String countryAName = "CountryA";
    String countryBName = "CountryB";
    String countryAId = UUID.randomUUID().toString();

    Neo4jCity cityA = Neo4jTestGenerator.neo4jCityNoRoutes("CityA", null);
    Neo4jCity cityB = Neo4jTestGenerator.neo4jCityNoRoutes("CityB", null);

    Neo4jCountry countryA = new Neo4jCountry(countryAId, countryAName, "short desc", "blob:url", Set.of(cityA, cityB));
    Neo4jCountry countryB = Neo4jTestGenerator.neo4jCountryNoCities(countryBName);

    @Test
    void getAll_shouldReturn200WrappedAllCountries_whenCountriesExist() throws Exception {

        Mockito.when(countryService.findAllCountries()).thenReturn(Set.of(countryA, countryB));

        mockMvc.perform(get("/neo4jCountries"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(Set.of(countryA, countryB))))
                .andReturn();
    }

    @Test
    void getAll_shouldReturn200_whenNoCountriesExist() throws Exception {

        Mockito.when(countryService.findAllCountries()).thenReturn(Set.of());

        mockMvc.perform(get("/neo4jCountries"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(Set.of())))
                .andReturn();
    }

    @Test
    void getCitiesGivenCountry_shouldReturn404_whenCountryDoesntExist() throws Exception {

        Mockito.when(countryService.getCitiesGivenCountry("not exist")).thenThrow(new NotFoundRequestException("Country not found"));

        mockMvc.perform(get(String.format("/neo4jCountries/%s/cities", "not exist")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Country not found"))
                .andReturn();
    }

    @Test
    void getCitiesGivenCountry_shouldReturn200_whenCountryExists() throws Exception {
        Mockito.when(countryService.getCitiesGivenCountry(countryAId)).thenReturn(Set.of(cityA, cityB));

        mockMvc.perform(get(String.format("/neo4jCountries/%s/cities", countryAId)))
                .andExpect(status().isOk())
                .andReturn();

        Mockito.when(countryService.getCitiesGivenCountry(countryAId)).thenReturn(Set.of(cityA, cityB));

        mockMvc.perform(get(String.format("/neo4jCountries/%s/cities", countryAId)))
                .andExpect(status().isOk())
                .andReturn();
    }
}