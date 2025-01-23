package com.nomad.backend.city.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.country.domain.Country;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.convert.ConvertWith;
import org.springframework.data.neo4j.core.schema.*;

import java.util.*;

@Node("City")
@Log4j2
public class City {

    @Id
    @Getter private final String id; // neo4j ids are strings
    @Getter private final String name;
    @Getter private final String description;

    @ConvertWith(converterRef = "cityMetricsConverter") // Uses a bean from Neo4jConfig.java
    @Getter private final CityMetrics cityMetrics;

    @Relationship(type = "ROUTE", direction = Relationship.Direction.OUTGOING)
    private final Set<Route> routes;

    @Relationship(type = "OF_COUNTRY", direction = Relationship.Direction.OUTGOING)
    @Getter private final Country country;

    // This factory is used by us
    public static City of(String name, String description, CityMetrics cityMetrics, Set<Route> routes, Country country) {
        return new City(null, name, description, cityMetrics, Set.copyOf(routes), country);
    }

    // This is used by Spring Data for object mapping
    public City(String id, String name, String description, CityMetrics cityMetrics, Set<Route> routes, Country country) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cityMetrics = cityMetrics;
        this.routes = routes;
        this.country = country;
    }

    // This is used by Neo4j for object mapping
    public City withId(String id) {
        return new City(id, this.name, this.description, this.cityMetrics, this.routes, this.country);
    }

    // Ensure mutable field 'routes' remains immutable
    public Set<Route> getRoutes() {
        return new HashSet<>(routes);
    }

    public City addRoute(Route route) {
        return addRoute(route.getTargetCity(), route.getPopularity(), route.getWeight(), route.getTransportType());
    }

    public City addRoute(City targetCity, int popularity, int weight, TransportType transportType) {
        Set<Route> existingRoutes = getRoutes();
        Route routeToAdd = Route.of(targetCity, popularity, weight, transportType);
        log.info("Adding route: {}", routeToAdd);

        Optional<Route> route = existingRoutes.stream()
                .filter(r -> Objects.equals(r.getTargetCity().getName(), targetCity.getName()) && r.getTransportType() == transportType)
                .findFirst();

        if (route.isPresent()) {
            Route existingRoute = route.get();
            if (!Objects.equals(existingRoute.getPopularity(), popularity) || !Objects.equals(existingRoute.getWeight(), weight)) {
                existingRoutes.remove(existingRoute);
                existingRoutes.add(routeToAdd);
            }
        } else {
            existingRoutes.add(routeToAdd);
        }
        return new City(this.id, this.name, this.description, this.cityMetrics, existingRoutes, this.country);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        log.warn("City routes: {} ", city.getRoutes().containsAll(routes));
        log.warn("This routes: {}", routes.containsAll(city.getRoutes()));
        return Objects.equals(id, city.id) && Objects.equals(name, city.name) && Objects.equals(description, city.description) && Objects.equals(cityMetrics, city.cityMetrics) && city.getRoutes().containsAll(routes) && routes.containsAll(city.getRoutes()) && Objects.equals(country, city.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, cityMetrics, routes, country);
    }

    @Override
    public String toString() {
        return "City{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cityMetrics=" + cityMetrics +
                ", routes=" + routes +
                ", country=" + country +
                '}';
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Map<String, Object> mapifyCity() throws JsonProcessingException {
        Map<String, Object> cityAsMap = OBJECT_MAPPER.convertValue(this, Map.class);
        // Stringify cityMetrics rather than creating a nested Hashmap, so it can be persisted in 1 neo4j field
        cityAsMap.put("cityMetrics", OBJECT_MAPPER.writeValueAsString(this.getCityMetrics()));
        return cityAsMap;
    }
}
