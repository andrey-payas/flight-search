package com.paias.air.model.n4j.projection;

import java.math.BigDecimal;

public interface AirportProjection {
    String getId();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
}
