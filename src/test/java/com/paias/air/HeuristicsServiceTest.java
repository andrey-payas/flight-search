package com.paias.air;

import com.paias.air.model.controller.FlightView;
import com.paias.air.model.n4j.heuristics.Route;
import com.paias.air.model.n4j.heuristics.CountryRoute;
import com.paias.air.search.Location;
import com.paias.air.repository.PathService;
import com.paias.air.service.HeuristicsService;
import com.paias.air.service.ItinerarySearchService;
import com.paias.air.service.SearchPreparationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

import static com.paias.air.WiremockUtils.stubCountries;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HeuristicsServiceTest extends TestBaseWiremock {
    @Autowired
    private HeuristicsService heuristicsService;
    @Autowired
    private ItinerarySearchService itinerarySearchService;
    @Autowired
    private SearchPreparationService searchPreparationService;
    @Autowired
    private PathService pathService;
    @Test
    public void prepareHeuristicsAndSearchFlights() throws IOException {
        stubCountries();
        YearMonth month = YearMonth.of(2028, 11);
        searchPreparationService.searchOutgoingForEachCountry(month, false);
        List<Route> routes = routeRepository.findAll();
        assertFalse(routes.isEmpty());

        heuristicsService.aggregateHeuristicsForEachCountry(month);
        List<CountryRoute> countryRoutes = countryRouteRepository.findAll();
        assertFalse(countryRoutes.isEmpty());

        pathService.findShortestPathsFromTo("GB", "DE", month);

        pathService.projectPathSearch("GB", month);
        pathService.projectPathSearch("DE", month);
        searchPreparationService.loadFlightsForPaths("GB", "DE", month, 1000);

        List<FlightView> flights = itinerarySearchService.searchItineraries(new Location("GB"), new Location("DE"), month.atDay(1), month.atEndOfMonth(),
                10, false, false, null, null, null, null, null, null);
        assertFalse(flights.isEmpty());
    }

    @Test
    public void findPaths() throws IOException {
        stubCountries();
        YearMonth month = YearMonth.of(2028, 11);
        searchPreparationService.searchOutgoingForEachCountry(month, false);
        heuristicsService.aggregateHeuristicsForEachCountry(month);
        pathService.findShortestPathsFromTo("GB", "DE", month);

        List<CountryRoute> countryRoutes = countryRouteRepository.findAll();
        assertFalse(countryRoutes.isEmpty());
    }
}
