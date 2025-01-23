package com.nomad.backend.country;

import com.nomad.backend.country.domain.Country;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CountryRepository extends Neo4jRepository<Country, String> {

    @Query("""
        MERGE (c:Country {name: $country.__properties__.name})
        ON CREATE SET c.id = randomUUID()
        SET c.description = $country.__properties__.description
    """)
    Country saveCountryWithDepth0(Country country);

    @Query("MATCH (country:Country {name: $countryName}) RETURN country")
    Optional<Country> findByName(String countryName);

    @Query("""
      MATCH(country:Country) RETURN country
    """)
    Set<Country> findAllCountries();

    @Query("""
      MATCH (country:Country {name: $countryName})
      OPTIONAL MATCH (country) -[rel:HAS_CITY]-> (cities:City)
      RETURN country, collect(rel), collect(cities)
    """)
    Optional<Country> findByNameFetchCities(String countryName);
}
