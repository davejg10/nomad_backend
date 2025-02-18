package com.nomad.backend.city.domain;

import lombok.Value;

@Value
public class CityMetric {

    CityCriteria criteria;
    double metric;
}
