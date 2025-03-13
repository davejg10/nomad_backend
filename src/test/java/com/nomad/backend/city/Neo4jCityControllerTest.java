package com.nomad.backend.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.exceptions.NotFoundRequestException;
import com.nomad.data_library.GenericTestGenerator;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = Neo4jCityController.class)
public class Neo4jCityControllerTest {

    @MockitoBean
    private Neo4jCityService cityService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    String countryId = "f1f9416f-0e7c-447c-938c-5d39cf10dad3";
    Neo4jCountry country = new Neo4jCountry(countryId, "CountryA", Set.of());

    String cityAId = "1226a656-0450-4156-a522-4ae588caa937";
    String cityBId = "f19c59be-a9b1-4fa4-a96c-e11f2bb111c0";

    
    Neo4jCity cityA = new Neo4jCity(cityAId, "CityA", GenericTestGenerator.cityMetrics(), Set.of(), country);
    Neo4jCity cityB = new Neo4jCity(cityBId, "CityB", GenericTestGenerator.cityMetrics(), Set.of(), country);

    @Test
    void getCity_shouldReturn200_whenCityExists() throws Exception {
        Mockito.when(cityService.getCity(cityAId, true)).thenReturn(cityA);
        mockMvc.perform(get(String.format("/neo4jCities/%s", cityAId))
                        .param("includeRoutes", "true"))
                .andExpect(status().isOk())
                .andReturn();

        Mockito.when(cityService.getCity(cityAId, false)).thenReturn(cityA);
        mockMvc.perform(get(String.format("/neo4jCities/%s", cityAId))
                        .param("includeRoutes", "false"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void getCity_shouldReturn404_whenCityNotFound() throws Exception {

        Mockito.when(cityService.getCity(cityAId, true)).thenThrow(new NotFoundRequestException("City not found"));

        MvcResult mvcResult = mockMvc.perform(get(String.format("/neo4jCities/%s", cityAId))
                        .param("includeRoutes", "true"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("City not found"))
                .andReturn();
    }

    @Test
    void getCityFetchRoutesWithCountryId_shouldReturn200_whenCityExists() throws Exception {
        cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityB));

        Mockito.when(cityService.getCityFetchRoutesWithCountryId(cityAId, countryId)).thenReturn(cityA);
        mockMvc.perform(get(String.format("/neo4jCities/%s/routes/%s", cityAId, countryId)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void getCityFetchRoutesWithCountryId_shouldReturn404_whenCityNotFound() throws Exception {

        Mockito.when(cityService.getCityFetchRoutesWithCountryId(cityAId, countryId)).thenThrow(new NotFoundRequestException("City not found"));

        MvcResult mvcResult = mockMvc.perform(get(String.format("/neo4jCities/%s/routes/%s", cityAId, countryId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("City not found"))
                .andReturn();
    }


}
