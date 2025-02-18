package com.nomad.backend.country;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.city.domain.City;
import com.nomad.backend.country.domain.Country;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.BiFunction;

@Repository
public class CountryRepository {

    private final Neo4jClient client;
    private final ObjectMapper objectMapper;
    private final BiFunction<TypeSystem, MapAccessor, City> cityMapper;
    private final BiFunction<TypeSystem, MapAccessor, Country> countryMapper;

    public CountryRepository(Neo4jClient client, ObjectMapper objectMapper, Neo4jMappingContext schema) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.cityMapper = schema.getRequiredMappingFunctionFor(City.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Country.class);
    }

    public Optional<Country> findById(String countryId) {
        return findById(countryId, false);
    }

    public Optional<Country> findByIdFetchCities(String countryId) {
        return findById(countryId, true);
    }

    private Optional<Country> findById(String countryId, boolean returnAllCities) {
        Optional<Country> country = client
                .query("""
                    MATCH (country:Country {id: $countryId})
                    OPTIONAL MATCH (country) -[hasCity:HAS_CITY]-> (cities:City)
                    RETURN country, collect(hasCity) as hasCity, collect(cities) as cities
                """)
                .bind(countryId).to("countryId")
                .fetchAs(Country.class)
                .mappedBy((typeSystem, record) -> {
                    Country fetchedCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<City> cities = new HashSet<>();
                    if (!record.get("hasCity").asList().isEmpty() && returnAllCities) {

                        record.get("cities").asList(city -> {
                            City createdTargetCity = cityMapper.apply(typeSystem, city.asNode());
                            createdTargetCity = new City(createdTargetCity.getId(), createdTargetCity.getName(), createdTargetCity.getDescription(), createdTargetCity.getCityMetrics(), createdTargetCity.getRoutes(), fetchedCountry);
                            cities.add(createdTargetCity);
                            return null;
                        });
                    }

                    return new Country(fetchedCountry.getId(), fetchedCountry.getName(), fetchedCountry.getDescription(), cities);
                })
                .first();
        return country;
    }

    public Set<Country> findAllCountries() {
        Collection<Country> allCountries = client
                .query("""
                    MATCH(country:Country) RETURN country
                """)
                .fetchAs(Country.class)
                .mappedBy((typeSystem, record) -> {
                    Country fetchedCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    return new Country(fetchedCountry.getId(), fetchedCountry.getName(), fetchedCountry.getDescription(), Set.of());
                })
                .all();
        return new HashSet<>(allCountries);
    }

    public Country saveCountryWithDepth0(Country country) {

        Map<String, Object> countryAsMap = objectMapper.convertValue(country, Map.class);

        Country createdCountry = client
                .query("""
                    MERGE (country:Country {name: $name})
                    ON CREATE SET country.id = randomUUID()
                    SET country.description = $description
                    RETURN country
                """)
                .bindAll(countryAsMap)
                .fetchAs(Country.class)
                .mappedBy((typeSystem, record) -> {
                    return countryMapper.apply(typeSystem, record.get("country").asNode());

                })
                .first()
                .get();

        return createdCountry;
    }
}