package com.nomad.backend.country;

import com.nomad.backend.city.domain.City;
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

    public Set<City> getCitiesGivenCountry(String countryId) throws NotFoundRequestException {
        log.info("Fetching cities given country country by id: {}", countryId);
        Optional<Country> country = countryRepository.findByIdFetchCities(countryId);

        if (country.isPresent()) {
            return country.get().getCities();
        } else {
            log.warn("Country with id: {}, not found.", countryId);
            throw new NotFoundRequestException("Country with id " + countryId + " not found.");
        }
    }

}
