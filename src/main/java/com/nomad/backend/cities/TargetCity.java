package com.nomad.backend.cities;

import org.springframework.data.neo4j.core.schema.Id;

import java.util.Set;

public class TargetCity {
    @Id
    private final String id;

    private final String name;
    private final String description;
    private final String countryName;

    public TargetCity(String id, String name, String description, String countryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.countryName = countryName;
    }

    // This is used by Neo4j for object mapping
    public TargetCity withId(String id) {
        return new TargetCity(id, this.name, this.description, this.countryName);
    }

}
