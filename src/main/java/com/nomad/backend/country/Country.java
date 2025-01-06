package com.nomad.backend.country;

import lombok.Data;

import java.util.UUID;

@Data
public class Country {
    private UUID id;
    private String name;
    private String description;

    public Country(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
    }

}
