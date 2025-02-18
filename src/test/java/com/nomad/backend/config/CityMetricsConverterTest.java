package com.nomad.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.TestConfig;
import com.nomad.backend.city.CityRepository;
import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.CountryRepository;
import com.nomad.backend.country.domain.Country;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@Import({TestConfig.class}) // For ObjectMapper
public class CityMetricsConverterTest {

    @Autowired
    private CityMetricsConverter cityMetricsConverter;

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .build();
    }


    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.neo4j.uri", embeddedDatabaseServer::boltURI);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "mypassword");
    }

    @AfterAll
    static void stopNeo4j() {
        embeddedDatabaseServer.close();
    }

    @Test
    void read_shouldConvertNeo4jValue_toCityMetricsObject() {
        Value neo4jCityMetricsProperty = Values.value("{\"sailing\":{\"criteria\":\"SAILING\",\"metric\":0},\"food\":{\"criteria\":\"FOOD\",\"metric\":6},\"nightlife\":{\"criteria\":\"NIGHTLIFE\",\"metric\":5}}\"");

        CityMetrics cityMetrics = cityMetricsConverter.read(neo4jCityMetricsProperty);

        assertThat(cityMetrics.getSailing().getMetric()).isEqualTo(0);
        assertThat(cityMetrics.getFood().getMetric()).isEqualTo(6);
        assertThat(cityMetrics.getNightlife().getMetric()).isEqualTo(5);

    }
}
