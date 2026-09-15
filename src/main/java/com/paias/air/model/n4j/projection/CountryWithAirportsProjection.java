package com.paias.air.model.n4j.projection;

import java.util.List;

public interface CountryWithAirportsProjection {
    String getId();
    List<AirportProjection> getAirports();
}
