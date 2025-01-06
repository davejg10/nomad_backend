package com.nomad.backend.country;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class CountryRepository {

    private List<Country> countries = IntStream.range(0, 12).boxed()
            .map(i -> new Country("Country" + i, ""))
            .collect(Collectors.toList());

    public List<Country> getAll() {
        return countries;
    }

}
