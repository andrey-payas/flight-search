package com.paias.air.model.n4j.projection;

import java.util.List;

public interface CountryProjection {
    String getId();
    List<IdProjection> getAirports();
}