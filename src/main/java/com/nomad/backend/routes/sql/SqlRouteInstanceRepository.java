package com.nomad.backend.routes.sql;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nomad.data_library.domain.sql.RouteInstance;

@Repository
public interface SqlRouteInstanceRepository extends CrudRepository<RouteInstance, UUID>{
    
    List<RouteInstance> findByRouteDefinitionIdInAndSearchDate(List<UUID> routeDefinitionIds, LocalDate searchDate);
}
