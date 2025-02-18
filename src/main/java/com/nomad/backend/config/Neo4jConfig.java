package com.nomad.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;

@Configuration
public class Neo4jConfig {

    @Bean
    org.neo4j.cypherdsl.core.renderer.Configuration cypherDslConfiguration() {
        return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig()
                .withDialect(Dialect.NEO4J_5).build();
    }

    @Bean
    public Neo4jTransactionManager transactionManager(org.neo4j.driver.Driver driver) {
        return new Neo4jTransactionManager(driver);
    }

    @Bean
    public CityMetricsConverter cityMetricsConverter(ObjectMapper objectMapper) {
        return new CityMetricsConverter(objectMapper);
    }

}