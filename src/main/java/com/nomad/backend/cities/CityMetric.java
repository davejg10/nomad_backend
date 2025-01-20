package com.nomad.backend.cities;

import lombok.Data;

@Data
public class CityMetric {

    private final CityCriteria criteria;
    private final int metric;

    public CityMetric(CityCriteria criteria, int metric) {
        this.criteria = criteria;
        this.metric = metric;
    }
}
