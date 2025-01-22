package com.nomad.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.cities.CityMetric;
import com.nomad.backend.cities.CityMetrics;
import lombok.extern.log4j.Log4j2;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.convert.Neo4jPersistentPropertyConverter;

import java.util.Map;

@Log4j2
public class CityMetricsConverter implements Neo4jPersistentPropertyConverter<CityMetrics> {

    private final ObjectMapper objectMapper;

    public CityMetricsConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Value write(CityMetrics source) {
        return null;
    }

    @Override
    public CityMetrics read(Value source) {
        try {
            String json = source.asString();
            Map<String, CityMetric> metricsMap = objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, CityMetric.class));

            return new CityMetrics(metricsMap.get("sailing"), metricsMap.get("food"), metricsMap.get("nightlife"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

