package com.nomad.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.city.domain.CityMetric;
import com.nomad.backend.city.domain.CityMetrics;
import com.nomad.backend.city.domain.Route;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.value.StringValue;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import com.nomad.backend.city.domain.City;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RouteConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> convertiblePairs = new HashSet<>();
        convertiblePairs.add(new ConvertiblePair(Value.class, Route.class));
//        convertiblePairs.add(new ConvertiblePair(Value.class, MyCustomType.class));
        return convertiblePairs;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return convertToRoute((Value) source);
    }

    public Route convertToRoute(Value source) {
        log.warn("In convertToRoute with source {}", source);

        return null;
    }

//    private Value convertToNeo4jValue(City source) {
//        log.warn("In city converter");
//        Map<String, Object> cityAsMap = null;
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            cityAsMap = objectMapper.convertValue(source, Map.class);
//            cityAsMap.put("cityMetrics", objectMapper.writeValueAsString(source.getCityMetrics()));
//            log.warn("citymap; {}", cityAsMap);
//        } catch (JsonProcessingException e) {
//
//            log.warn("There was an issue converting {} toNeo4jValue with error: {}", source, e.getMessage());
//        }
//        return Values.value(cityAsMap);
//
//    }

}
