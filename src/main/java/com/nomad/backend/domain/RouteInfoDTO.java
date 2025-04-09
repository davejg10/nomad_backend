package com.nomad.backend.domain;

import com.nomad.data_library.domain.neo4j.Neo4jRoute;

public record RouteInfoDTO(Neo4jRoute neo4jRoute, double score, double distance) {}
