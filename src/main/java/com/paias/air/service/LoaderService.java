package com.paias.air.service;

import com.paias.air.client.TequilaClient;
import com.paias.air.model.n4j.Flight;
import com.paias.air.model.n4j.heuristics.Country;
import com.paias.air.repository.FlightRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

@Slf4j
@Service
public class LoaderService {
    private static final long MAX_PRICE = 800L;
    private static final int COUNTRY_BATCH_SIZE = 70;
    private final TequilaClient client;
    private final FlightRepository flightRepository;
    private final HeuristicsService heuristicsService;
    private final TransactionTemplate transactionTemplate;
    @Getter
    @Value("#{'${country.list}'.split(',')}")
    private List<String> topCountries;

    public LoaderService(TequilaClient client, FlightRepository flightRepository,
                         HeuristicsService heuristicsService,
                         TransactionTemplate transactionTemplate) {
        this.client = client;
        this.flightRepository = flightRepository;
        this.heuristicsService = heuristicsService;
        this.transactionTemplate = transactionTemplate;
    }

    public void loadOutgoingForCountry(YearMonth month, Country country, boolean allCountries) {
        List<Flight> flights = client.search(country.getId(), month, true);
        log.info("Found {} flights for country {}", flights.size(), country.getId());
        saveFlightsFromCountry(flights, country.getId(), month);
        if (!allCountries) {
            return;
        }
        List<String> missingCountries = new ArrayList<>(topCountries);
        missingCountries.removeAll(flights.stream()
                .map(Flight::getToCountryCode)
                .collect(Collectors.toSet()));

        List<List<String>> batches = missingCountries.stream().gather(Gatherers.windowFixed(COUNTRY_BATCH_SIZE)).toList();
        for (List<String> batch : batches) {
            flights = client.search(country.getId(), String.join(",", batch), MAX_PRICE, month, true);
            log.info("Found {} flights for countries {}", flights.size(), country.getId());
            saveFlightsFromCountry(flights, country.getId(), month);
        }
    }

    public void loadForMonth(String flyFrom, String flyTo, YearMonth month, Long maxPrice) {
        log.info("Loading flights from {} to {} at {} with max price {}", flyFrom, flyTo, month, maxPrice);
        List<Flight> flights = client.search(flyFrom, flyTo, maxPrice, month);
        saveFlightsFromCountry(flights, flyFrom, month);
    }

    public void loadForMonth(String flyFrom, String flyTo, YearMonth month) {
        List<Flight> flights = client.searchForMonth(flyFrom, flyTo, month);
        saveFlightsFromCountry(flights, flyFrom, month);
    }

    public void load(String flyFrom, String dateFrom, String dateTo) {
        List<Flight> flights = client.search(flyFrom, dateFrom, dateTo);
        saveFlights(flights);
    }

    public void saveFlights(List<Flight> flights) {
        transactionTemplate.executeWithoutResult(_ -> {
            log.info("Saving {} flights", flights.size());
            flightRepository.saveAll(flights);
            log.info("Aggregating {} flights", flights.size());
            heuristicsService.aggregateFlights(flights);
        });
    }

    public void saveFlightsFromCountry(List<Flight> flights, String countryCode, YearMonth month) {
        transactionTemplate.executeWithoutResult(_ -> {
            log.info("Saving {} flights from country {} for month {}", flights.size(), countryCode, month);
            flightRepository.saveAll(flights);
            log.info("Aggregating {} flights from country {} for month {}", flights.size(), countryCode, month);
            heuristicsService.aggregateFlightsFromCountry(flights, countryCode, month);
        });
    }
}
