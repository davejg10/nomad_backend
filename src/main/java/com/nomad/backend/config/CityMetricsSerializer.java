package com.nomad.backend.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nomad.backend.city.domain.CityMetrics;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

//This class is used to convert the CityMetrics object to a JSON string so it can be stored as a single Neo4j value
// (rather than a map of maps which object mapper would usually do)
@Slf4j
public class CityMetricsSerializer extends JsonSerializer<CityMetrics> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public CityMetricsSerializer() {
    }

    @Override
    public void serialize(CityMetrics value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(OBJECT_MAPPER.writeValueAsString(value));
    }
}