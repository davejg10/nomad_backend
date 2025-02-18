package com.nomad.backend.city.domain;

import lombok.Value;

@Value
public class CityMetrics {

    CityMetric sailing;
    CityMetric food;
    CityMetric nightlife;
}
