package com.nomad.backend.city.domain;

import com.nomad.backend.country.domain.Country;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@ExtendWith(MockitoExtension.class)
public class CityTest {

    @Mock
    CityMetrics cityMetrics;
    @Mock
    Country country;

    @Test
    void addRoute_shouldAddRoute_whenRouteDoesntExist() {
        City city = City.of("CityA", "", cityMetrics, Set.of(), country);
        City targetCity = City.of("CityB", "", cityMetrics, Set.of(), country);

        assertThat(city.getRoutes()).isEmpty();

        Route route = Route.of(targetCity, 2, 4, 16.0, TransportType.BUS);
        city = city.addRoute(route);

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);
    }

    @Test
    void addRoute_shouldNotAddRoute_whenRouteAlreadyExists() {
        City city = City.of("CityA", "", cityMetrics, Set.of(), country);
        City targetCity = City.of("CityB", "", cityMetrics, Set.of(), country);

        assertThat(city.getRoutes()).isEmpty();

        Route route = Route.of(targetCity, 2, 4, 16.0, TransportType.BUS);
        city = city.addRoute(route);

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);

        city = city.addRoute(route);
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);
    }

    @Test
    void addRoute_shouldOverwriteRoute_whenRouteToAddHasDifferentMetricsButSameTargetAndSameTransportType() {
        City city = City.of("CityA", "", cityMetrics, Set.of(), country);
        City targetCity = City.of("CityB", "", cityMetrics, Set.of(), country);

        assertThat(city.getRoutes()).isEmpty();

        Route route = Route.of(targetCity, 2, 4, 16.0, TransportType.BUS);
        city = city.addRoute(route);

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);

        Route lessPopularRoute = Route.of(route.getTargetCity(), route.getPopularity() - 1, route.getTime(), route.getCost(), route.getTransportType());

        city = city.addRoute(lessPopularRoute);
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(lessPopularRoute);

        Route longerRoute = Route.of(lessPopularRoute.getTargetCity(), lessPopularRoute.getPopularity(), lessPopularRoute.getTime() + 4, lessPopularRoute.getCost(), lessPopularRoute.getTransportType());

        city = city.addRoute(longerRoute);
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(longerRoute);

        Route moreExpensiveRoute = Route.of(longerRoute.getTargetCity(), longerRoute.getPopularity(), longerRoute.getTime(), longerRoute.getCost() + 10.0, longerRoute.getTransportType());

        city = city.addRoute(moreExpensiveRoute);
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(moreExpensiveRoute);
    }

}
