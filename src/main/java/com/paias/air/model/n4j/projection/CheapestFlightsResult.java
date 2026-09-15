package com.paias.air.model.n4j.projection;

import com.paias.air.model.n4j.Flight;

import java.util.List;

public record CheapestFlightsResult(
        String id,
        List<Flight> flights
) {}