package com.nomad.backend.country.domain;

import com.nomad.backend.city.domain.City;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Node("Country")
public class Country {

    @Id @Getter private final String id;
    @Getter private final String name;
    @Getter private final String description;

    @Relationship(type = "HAS_CITY", direction = Relationship.Direction.OUTGOING)
    private final Set<City> cities;

    public static Country of(String name, String description, Set<City> cities) {
        return new Country(null, name, description, Set.copyOf(cities));
    }

    public Country(String id, String name, String description, Set<City> cities) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cities = cities;
    }

    public Country withId(String id) {
        return new Country(id, this.name, this.description, this.cities);
    }

    // Ensure mutable field 'cities' remains immutable
    public Set<City> getCities() {
        return new HashSet<>(cities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(id, country.id) && Objects.equals(name, country.name) && Objects.equals(description, country.description) && Objects.equals(cities, country.cities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, cities);
    }

    @Override
    public String toString() {
        return "Country{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cities=" + cities +
                '}';
    }
}
