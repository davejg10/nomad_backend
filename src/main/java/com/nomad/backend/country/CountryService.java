package com.nomad.backend.country;

import com.nomad.backend.country.domain.Country;
import com.nomad.backend.exceptions.NotFoundRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Log4j2
@Service
public class CountryService {

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public Set<Country> findAllCountries() {
        log.info("Fetching all countries");
        return countryRepository.findAllCountries();
    }

    public Country getCountryByName(String countryName, boolean returnAllCities) throws NotFoundRequestException {
        log.info("Fetching country by name: {}, with returnAllCities: {}", countryName, returnAllCities);
        Optional<Country> country = returnAllCities ? countryRepository.findByNameFetchCities(countryName) : countryRepository.findByName(countryName);

        if (country.isPresent()) {
            return country.get();
        } else {
            log.warn("Country not found with name: {}", countryName);
            throw new NotFoundRequestException("Country " + countryName + " not found.");
        }
    }

}
