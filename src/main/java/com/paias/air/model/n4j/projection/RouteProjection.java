package com.paias.air.model.n4j.projection;

import java.time.YearMonth;

public interface RouteProjection {
    String getId();
    int getConfidence();
    YearMonth getMonth();
    IdProjection getFrom();
    IdProjection getTo();
}
