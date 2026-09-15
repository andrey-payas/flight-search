package com.paias.air.model.n4j.projection;

import com.paias.air.model.n4j.heuristics.Airport;
import com.paias.air.model.n4j.heuristics.Route;
import com.paias.air.model.n4j.heuristics.Country;

public record RouteRow(
    Route route,
    Airport fromAirport,
    Airport toAirport,
    Country toCountry
) {}
