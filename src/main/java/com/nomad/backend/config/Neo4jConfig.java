package com.nomad.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;

import java.util.Collections;
import java.util.Set;

@Configuration
@EnableTransactionManagement
public class Neo4jConfig {

    @Bean
    public Neo4jTransactionManager transactionManager(org.neo4j.driver.Driver driver) {
        return new Neo4jTransactionManager(driver);
    }

    @Bean
    public CityMetricsConverter cityMetricsConverter(ObjectMapper objectMapper) {
        return new CityMetricsConverter(objectMapper);
    }
}