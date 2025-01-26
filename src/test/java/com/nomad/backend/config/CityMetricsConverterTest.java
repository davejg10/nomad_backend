package com.nomad.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.TestConfig;
import com.nomad.backend.city.domain.CityMetrics;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

//@Import(TestConfig.class)
//public class CityMetricsConverterTest {
//
//    private CityMetricsConverter cityMetricsConverter = new CityMetricsConverter(new ObjectMapper());
//
//
//    @Test
//    void read_shouldConvertNeo4jValue_toCityMetricsObject() {
//        Value neo4jCityMetricsProperty = Values.value("{\"sailing\":{\"criteria\":\"SAILING\",\"metric\":0},\"food\":{\"criteria\":\"FOOD\",\"metric\":6},\"nightlife\":{\"criteria\":\"NIGHTLIFE\",\"metric\":5}}\"");
//
//        CityMetrics cityMetrics = cityMetricsConverter.read(neo4jCityMetricsProperty);
//
//        assertThat(cityMetrics.getSailing().getMetric()).isEqualTo(0);
//        assertThat(cityMetrics.getFood().getMetric()).isEqualTo(6);
//        assertThat(cityMetrics.getNightlife().getMetric()).isEqualTo(5);
//
//    }
//}
