package com.nomad.backend.routes.sql;

import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import com.nomad.data_library.domain.sql.RouteDefinition;

@Repository
public interface SqlRouteDefinitionRepository extends CrudRepository<RouteDefinition, UUID> {
    
}