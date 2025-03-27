package com.nomad.backend.country.neo4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.repositories.Neo4jCommonCountryMappers;
import com.nomad.data_library.repositories.Neo4jCommonCountryRepository;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

@Repository
public class Neo4jCountryRepository extends Neo4jCommonCountryRepository {

    public Neo4jCountryRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCommonCountryMappers neo4jCountryMappers) {
        super(neo4jClient, objectMapper, neo4jCountryMappers);
    }

}