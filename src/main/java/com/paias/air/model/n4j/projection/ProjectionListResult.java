package com.paias.air.model.n4j.projection;

import java.time.OffsetDateTime;

public record ProjectionListResult (
        String graphName,
        long nodeCount,
        long relationshipCount,
        OffsetDateTime creationTime,
        long memoryUsage
) {}