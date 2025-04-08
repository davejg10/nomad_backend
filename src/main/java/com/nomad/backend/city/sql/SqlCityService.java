package com.nomad.backend.city.sql;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nomad.backend.exceptions.NotFoundRequestException;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.repositories.SqlCityRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SqlCityService {

    private SqlCityRepository sqlCityRepository;

    public SqlCityService(SqlCityRepository sqlCityRepository) {
        this.sqlCityRepository = sqlCityRepository;
    }

    public SqlCity findById(String id) throws NotFoundRequestException {
        log.info("Fetching SqlCity with ID: {}.", id);
        Optional<SqlCity> sqlCity = sqlCityRepository.findById(UUID.fromString(id));

        if (sqlCity.isPresent()) {
            return sqlCity.get();
        } else {
            log.warn("SqlCity with ID {} not found.", id);
            throw new NotFoundRequestException("The SqlCity with id: " + id + " was not found");
        }
    }
}
