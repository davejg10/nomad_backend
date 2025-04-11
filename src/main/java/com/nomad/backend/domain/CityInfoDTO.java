package com.nomad.backend.domain;

import com.nomad.data_library.domain.neo4j.Neo4jCity;

public record CityInfoDTO(Neo4jCity city, double score) {}
