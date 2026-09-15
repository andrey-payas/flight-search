package com.paias.air;


import com.paias.air.model.n4j.Flight;
import com.paias.air.model.n4j.heuristics.Route;
import com.paias.air.service.LoaderService;
import com.paias.air.service.ItinerarySearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.paias.air.WiremockUtils.stubSearch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LoaderServiceTests extends TestBaseWiremock {
    @Autowired
    LoaderService service;
    @Autowired
    ItinerarySearchService itinerarySearchService;

    @Test
    void testLoadingNoDuplicates() {
        stubSearch("responses/search.json");
        service.load("PRG", "01/11/2028", "03/11/2028");
        service.load("PRG", "01/11/2028", "03/11/2028");
        List<Flight> flights = flightRepository.findAll();
        assertEquals(10, flights.size());
    }


    @Test
    void testLoadingHeuristics() {
        stubSearch("responses/search.json");
        service.load("PRG", "01/11/2028", "03/11/2028");
        List<Route> routes = routeRepository.findAll();
        assertFalse(routes.isEmpty());
    }
}
