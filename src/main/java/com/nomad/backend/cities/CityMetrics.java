package com.nomad.backend.cities;

import lombok.Data;

@Data
public class CityMetrics {

    private final CityMetric sailing;
    private final CityMetric food;
    private final CityMetric nightlife;
}
