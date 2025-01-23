package com.nomad.backend.city;

import com.nomad.backend.city.domain.*;
import com.nomad.backend.country.domain.Country;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CityTest {

    @Mock CityMetrics cityMetrics;
    @Mock Country country;

    @Test
    void addRoute_ShouldAddARoute_whenRouteDoesntExist() {
        City city =  City.of("CityA", "", cityMetrics, Set.of(), country);
        City targetCity =  City.of("CityB", "", cityMetrics, Set.of(), country);

        assertThat(city.getRoutes()).isEmpty();

        Route route = Route.of(targetCity, 2, 4, TransportType.BUS);
        city = city.addRoute(route.getTargetCity(), route.getPopularity(), route.getWeight(), route.getTransportType());

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);
    }

    @Test
    void addRoute_ShouldNotAddARoute_whenRouteAlreadyExists() {
        City city =  City.of("CityA", "", cityMetrics, Set.of(), country);
        City targetCity =  City.of("CityB", "", cityMetrics, Set.of(), country);

        assertThat(city.getRoutes()).isEmpty();

        Route route = Route.of(targetCity, 2, 4, TransportType.BUS);
        city = city.addRoute(route.getTargetCity(), route.getPopularity(), route.getWeight(), route.getTransportType());

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);

        city = city.addRoute(route.getTargetCity(), route.getPopularity(), route.getWeight(), route.getTransportType());
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);
    }

    @Test
    void addRoute_ShouldOverwriteRoute_whenRouteToAddHasDifferentMetricsButSameTargetAndSameTransportType() {
        City city =  City.of("CityA", "", cityMetrics, Set.of(), country);
        City targetCity =  City.of("CityB", "", cityMetrics, Set.of(), country);

        assertThat(city.getRoutes()).isEmpty();

        Route route = Route.of(targetCity, 2, 4, TransportType.BUS);
        city = city.addRoute(route.getTargetCity(), route.getPopularity(), route.getWeight(), route.getTransportType());

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);

        Route lessPopularRoute = Route.of(targetCity, 1, 4, TransportType.BUS);

        city = city.addRoute(lessPopularRoute.getTargetCity(), lessPopularRoute.getPopularity(), lessPopularRoute.getWeight(), lessPopularRoute.getTransportType());
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(lessPopularRoute);
    }

}
