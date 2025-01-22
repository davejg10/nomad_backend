package com.nomad.backend.cities;

import lombok.Data;

@Data
public class CityMetric {

    private final CityCriteria criteria;
    private final int metric;
}
