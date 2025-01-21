package com.nomad.backend.cities;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Node("City")
@Log4j2
public class City {

    @Getter
    @Id
    private final String id; // neo4j ids are strings

    @Getter
    private final String name;

    @Getter
    private final String description;
    @Getter
    private final String countryName;

    @Relationship(type = "ROUTE", direction = Relationship.Direction.OUTGOING)
    private final Set<Route> routes;

    // This factory is used by us
    public static City of(String name, String description, String countryName, Set<Route> routes) {
        return new City(null, name, description, countryName, Set.copyOf(routes));
    }

    // This is used by Spring Data for object mapping
    public City(String id, String name, String description, String countryName, Set<Route> routes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.countryName = countryName;
        this.routes = routes;
    }

    // This is used by Neo4j for object mapping
    public City withId(String id) {
        return new City(id, this.name, this.description, this.countryName, this.routes);
    }

    // Ensure mutable field 'routes' remains immutable
    public Set<Route> getRoutes() {
        return new HashSet<>(routes);
    }

    public City addRoute(Route route) {
        return addRoute(route.getTargetCity(), route.getPopularity(), route.getWeight(), route.getTransportType());
    }

    public City addRoute(City targetCity, String popularity, String weight, TransportType transportType) {
        Set<Route> existingRoutes = getRoutes();
        Route routeToAdd = Route.of(targetCity, popularity, weight, transportType);

        Optional<Route> routeExists = existingRoutes.stream()
                .filter(route -> Objects.equals(route.getTargetCity().getName(), targetCity.getName()) && route.getTransportType() == transportType)
                .findFirst();

        if (routeExists.isPresent()) {
            Route route = routeExists.get();
            if (!Objects.equals(route.getPopularity(), popularity) || !Objects.equals(route.getWeight(), weight)) {
                existingRoutes.remove(route);
                existingRoutes.add(routeToAdd);
                log.warn("Route with same transport type already exists but popularity or weight are different");
                log.warn("Old route: " + route);
                log.warn("New route: " + routeToAdd);
                return new City(this.id, this.name, this.description, this.countryName, existingRoutes);
            } else {
                log.warn("Route exists already, doing nothing.....");
                return this;
            }
        } else {
            existingRoutes.add(routeToAdd);
            log.info("Route does not existing, adding route...");
            log.info("New route: " + routeToAdd);
            return new City(this.id, this.name, this.description, this.countryName, existingRoutes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(id, city.id) && Objects.equals(name, city.name) && Objects.equals(description, city.description) && Objects.equals(countryName, city.countryName) && Objects.equals(routes, city.routes);
    }

    @Override
    public String toString() {
        return "City{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", countryName='" + countryName + '\'' +
                ", routes=" + routes +
                '}';
    }
}
