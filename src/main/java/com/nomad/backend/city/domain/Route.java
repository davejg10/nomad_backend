package com.nomad.backend.city.domain;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.schema.*;

import java.util.Objects;

@Getter
@RelationshipProperties
@Log4j2
public class Route {

    @RelationshipId
    private final String id;

    private final double popularity;
    private final double time;
    private final TransportType transportType;

    @TargetNode
    private final City targetCity;

    // This factory is used by us
    public static Route of(City targetCity, double popularity, double time, TransportType transportType) {
        return new Route(null, targetCity, popularity, time, transportType);
    }

    // This is used by Spring data for object mapping
    public Route(String id, City targetCity, double popularity, double time, TransportType transportType) {
        this.id = id;
        this.targetCity = targetCity;
        this.popularity = popularity;
        this.time = time;
        this.transportType = transportType;
    }

    // This is used by Neo4j for object mapping
    public Route withId(String id) {
        return new Route(id, this.targetCity, this.popularity, this.time, this.transportType);
    }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", popularity=" + popularity +
                ", time=" + time +
                ", transportType=" + transportType +
                ", targetCity=" + targetCity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(id, route.id) && popularity == route.popularity && time == route.time && transportType == route.transportType && Objects.equals(targetCity.getName(), route.getTargetCity().getName()) && Objects.equals(targetCity.getId(), route.getTargetCity().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, popularity, time, transportType, targetCity.getName(), targetCity.getId());
    }
}
