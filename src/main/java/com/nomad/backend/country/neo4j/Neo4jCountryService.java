package com.nomad.backend.country.neo4j;

import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.backend.exceptions.NotFoundRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Log4j2
@Service
public class Neo4jCountryService {

    private final Neo4jCountryRepository neo4jCountryRepository;

    public Neo4jCountryService(Neo4jCountryRepository neo4jCountryRepository) {
        this.neo4jCountryRepository = neo4jCountryRepository;
    }

    public Set<Neo4jCountry> findAllCountries() {
        log.info("Fetching all countries");
        return neo4jCountryRepository.findAllCountries();
    }

    public Set<Neo4jCity> getCitiesGivenCountry(String countryId) throws NotFoundRequestException {
        log.info("Fetching cities given Neo4jCountry by id: {}", countryId);
        Optional<Neo4jCountry> neo4jCountry = neo4jCountryRepository.findByIdFetchCities(countryId);

        if (neo4jCountry.isPresent()) {
            return neo4jCountry.get().getCities();
        } else {
            log.warn("Neo4jCountry with id: {}, not found.", countryId);
            throw new NotFoundRequestException("Neo4jCountry with id " + countryId + " not found.");
        }
    }

}
