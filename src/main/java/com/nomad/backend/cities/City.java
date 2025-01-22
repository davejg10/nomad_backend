package com.nomad.backend.cities;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.convert.ConvertWith;
import org.springframework.data.neo4j.core.schema.*;

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

    @Getter
    @ConvertWith(converterRef = "cityMetricsConverter") // Uses a bean from Neo4jConfig.java
    private final CityMetrics cityMetrics;

    @Relationship(type = "ROUTE", direction = Relationship.Direction.OUTGOING)
    private final Set<Route> routes;

    // This factory is used by us
    public static City of(String name, String description, String countryName, CityMetrics cityMetrics, Set<Route> routes) {
        return new City(null, name, description, countryName, cityMetrics, Set.copyOf(routes));
    }

    // This is used by Spring Data for object mapping
    public City(String id, String name, String description, String countryName, CityMetrics cityMetrics, Set<Route> routes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.countryName = countryName;
        this.cityMetrics = cityMetrics;
        this.routes = routes;
    }

    // This is used by Neo4j for object mapping
    public City withId(String id) {
        return new City(id, this.name, this.description, this.countryName, this.cityMetrics, this.routes);
    }

    // Ensure mutable field 'routes' remains immutable
    public Set<Route> getRoutes() {
        return new HashSet<>(routes);
    }

    public City addRoute(City targetCity, int popularity, int weight, TransportType transportType) {
        Set<Route> existingRoutes = getRoutes();
        Route routeToAdd = Route.of(targetCity, popularity, weight, transportType);

        Optional<Route> route = existingRoutes.stream()
                .filter(r -> Objects.equals(r.getTargetCity().getName(), targetCity.getName()) && r.getTransportType() == transportType)
                .findFirst();

        if (route.isPresent()) {
            Route existingRoute = route.get();
            if (!Objects.equals(existingRoute.getPopularity(), popularity) || !Objects.equals(existingRoute.getWeight(), weight)) {
                existingRoutes.remove(existingRoute);
                existingRoutes.add(routeToAdd);
            } else {
                existingRoutes.remove(existingRoute);
            }
        } else {
            existingRoutes.add(routeToAdd);
        }
        return new City(this.id, this.name, this.description, this.countryName, this.cityMetrics, existingRoutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(id, city.id) && Objects.equals(name, city.name) && Objects.equals(description, city.description) && Objects.equals(countryName, city.countryName) && Objects.equals(cityMetrics, city.cityMetrics) && Objects.equals(routes, city.routes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, countryName, cityMetrics, routes);
    }

    @Override
    public String toString() {
        return "City{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", countryName='" + countryName + '\'' +
                ", cityMetrics=" + cityMetrics +
                ", routes=" + routes +
                '}';
    }
}
