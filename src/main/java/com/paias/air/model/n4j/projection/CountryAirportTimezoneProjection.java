package com.paias.air.model.n4j.projection;

import java.util.List;

public interface CountryAirportTimezoneProjection {
    String getId();
    List<AirportTimezoneProjection> getAirports();
}