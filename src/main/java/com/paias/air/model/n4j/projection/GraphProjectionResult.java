package com.paias.air.model.n4j.projection;

public record GraphProjectionResult(
    String graph,
    long nodeCount,
    long relationshipCount
) {}