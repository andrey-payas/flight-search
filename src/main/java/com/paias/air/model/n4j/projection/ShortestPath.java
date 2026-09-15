package com.paias.air.model.n4j.projection;

import java.util.List;

public record ShortestPath(
        Integer index,
        Double totalCost,
        List<String> route
) {

}
