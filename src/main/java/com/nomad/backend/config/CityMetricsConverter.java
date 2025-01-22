package com.nomad.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.cities.CityMetrics;
import lombok.extern.log4j.Log4j2;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.data.neo4j.core.convert.Neo4jPersistentPropertyConverter;

@Log4j2
public class CityMetricsConverter implements Neo4jPersistentPropertyConverter<CityMetrics> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Value write(CityMetrics source) {
        try {
            return Values.value(objectMapper.writeValueAsString(source));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert CityMetrics to JSON", e);
        }
    }

    @Override
    public CityMetrics read(Value source) {
        log.warn("In converter READ: WITH SOURCE: " + source);
        return null;
    }
}

