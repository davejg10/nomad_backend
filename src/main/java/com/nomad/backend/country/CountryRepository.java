package com.nomad.backend.country;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class CountryRepository {

    private static List<Country> countries = IntStream.range(0, 12).boxed()
            .map(i -> new Country("Country" + i, ""))
            .collect(Collectors.toList());

//    public static List<UUID> countryUUIDs = countries.stream().map(Country::getId).toList();


    public List<Country> getAll() {
        return countries;
    }

}
