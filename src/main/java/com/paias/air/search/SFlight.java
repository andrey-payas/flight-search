package com.paias.air.search;

import com.paias.air.model.n4j.Flight;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SFlight {
    final Flight flight;
    final int fromAirport;
    final int fromTime;
    final int toAirport;
    final int toTime;
    // price in cents
    final int price;
    final int hops;

    public SFlight(Flight flight, int fromAirport, int fromTime, int toAirport, int toTime, int price, int hops) {
        this.flight = flight;
        this.fromAirport = fromAirport;
        this.fromTime = fromTime;
        this.toAirport = toAirport;
        this.toTime = toTime;
        this.price = price;
        this.hops = hops;
    }
}
