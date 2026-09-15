package com.paias.air.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paias.air.model.client.CountryToData;
import com.paias.air.model.client.FlightData;
import com.paias.air.model.client.LegData;
import com.paias.air.model.n4j.Flight;
import com.paias.air.model.n4j.Leg;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FlightConverter {
    private final ObjectMapper objectMapper;
    @SneakyThrows
    public Flight flightDataToFlight(FlightData flightData) {
        Long price = flightData.getPrice();
        List<Leg> legs = flightData.getRoute().stream().map(this::legDataToLeg).toList();
        return new Flight(flightData.getId(),flightData.getFlyFrom(), flightData.getUtcDeparture(),
                flightData.getFlyTo(), flightData.getUtcArrival(),
                price, legs, getCountryCode(flightData.getCountryTo()), flightData.getDeepLink(),legs.size());
    }

    private String getCountryCode(CountryToData countryTo) {
        return countryTo != null ? countryTo.getCode() : null;
    }

    private Leg legDataToLeg(LegData legData) {
        Leg leg = new Leg();
        leg.setId(legData.getId());
        leg.setFlyFrom(legData.getFlyFrom());
        leg.setFlyTo(legData.getFlyTo());
        leg.setUtcDeparture(legData.getUtcDeparture());
        leg.setUtcArrival(legData.getUtcArrival());
        leg.setFlightNo(legData.getFlightNo());
        return leg;
    }
}
