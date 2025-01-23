package com.nomad.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.config.CityMetricsConverter;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({JacksonAutoConfiguration.class}) // For ObjectMapper
public class TestConfig {


    @Bean
    public CityMetricsConverter cityMetricsConverter(ObjectMapper objectMapper) {
        return new CityMetricsConverter(objectMapper);
    }
}