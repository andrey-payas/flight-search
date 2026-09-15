package com.paias.air;

import com.paias.air.model.n4j.Flight;
import com.paias.air.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FlightRepositoryTest extends TestBaseWiremock {
    public static final String FLIGHT_ID = "FLIGHT-1";
    @Autowired
    private FlightRepository flightRepository;

    @Test
    public void testFlightRepository() {
        Flight flight = new Flight();
        flight.setId(FLIGHT_ID);
        flight.setPrice(10L);
        flight.setFromAirport("PRG");
        flight.setToAirport("BRL");
        flight.setFromTime(LocalDateTime.now());
        flight.setToTime(LocalDateTime.now());

        flightRepository.save(flight);

        YearMonth now = YearMonth.now();

        String dateFrom = now.minusMonths(1).atDay(1).toString();
        String dateTo = now.plusMonths(1).atDay(1).toString();

        List<Flight> flights = flightRepository.findFlightsForAirportsBy(
                List.of("PRG", "BRL"),
                dateFrom,
                dateTo
        );

        assertNotNull(flights);
        assertNotNull(flights.getFirst());
    }
}