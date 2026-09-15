package com.paias.air.model.n4j.projection;

import java.util.List;

public record CountryShortestPath(
        String from,
        String to,
        Double totalCost,
        Double totalRawCost,
        List<String> route
) {}
