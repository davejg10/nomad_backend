package com.nomad.backend.cities;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.schema.*;

import java.util.Objects;

@Getter
@RelationshipProperties
@Log4j2
public class Route {

    @RelationshipId
    @GeneratedValue
    private final String id;

    private final int popularity;
    private final int weight;
    private final TransportType transportType;

    @TargetNode
    private final City targetCity;

    // This factory is used by us
    public static Route of(City targetCity, int popularity, int weight, TransportType transportType) {
        return new Route(null, targetCity, popularity, weight, transportType);
    }

    // This is used by Spring data for object mapping
    public Route(String id, City targetCity, int popularity, int weight, TransportType transportType) {
        this.id = id;
        this.targetCity = targetCity;
        this.popularity = popularity;
        this.weight = weight;
        this.transportType = transportType;
    }

    // This is used by Neo4j for object mapping
    public Route withId(String id) {
        return new Route(id, this.targetCity, this.popularity, this.weight, this.transportType);
    }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", popularity=" + popularity +
                ", weight=" + weight +
                ", transportType=" + transportType +
                ", targetCity=" + targetCity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return popularity == route.popularity && weight == route.weight && Objects.equals(id, route.id) && transportType == route.transportType && Objects.equals(targetCity, route.targetCity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, popularity, weight, transportType, targetCity);
    }
}
