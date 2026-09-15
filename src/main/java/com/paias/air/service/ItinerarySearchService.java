package com.paias.air.service;

import com.paias.air.client.TequilaClient;
import com.paias.air.model.controller.CostWeights;
import com.paias.air.model.controller.FlightView;
import com.paias.air.model.n4j.Flight;
import com.paias.air.model.n4j.heuristics.Airport;
import com.paias.air.model.n4j.heuristics.Country;
import com.paias.air.model.n4j.projection.*;
import com.paias.air.repository.CountryRepository;
import com.paias.air.repository.FlightSearchRepository;
import com.paias.air.repository.FlightRepository;
import com.paias.air.repository.PathService;
import com.paias.air.search.Location;
import com.paias.air.search.LocationCodeMapper;
import com.paias.air.search.AirportConnectionFinder;
import com.paias.air.search.DijkstraSearch;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.paias.air.util.ProjectionUtils.as;
import static com.paias.air.util.ProjectionUtils.copyFieldsIgnoreUnknown;

@Slf4j
@Service
public class ItinerarySearchService {

    private static final int DEFAULT_HOP_PENALTY = 1000;
    private static final int DEFAULT_LAYOVER_PENALTY_PER_HOUR = 50;
    private static final int DEFAULT_SHORT_LAYOVER_PENALTY = 0;
    private static final int DEFAULT_FLIGHT_TIME_PENALTY_PER_HOUR = 200;

    private final PathService pathService;

    private final LoaderService loaderService;
    private final CountryRepository countryRepository;
    private final FlightRepository flightRepository;
    private final FlightSearchRepository flightSearchRepository;
    private final LocationCodeMapper locationCodeMapper;
    private final AirportConnectionFinder airportConnectionFinder;
    private final TequilaClient tequilaClient;

    public ItinerarySearchService(PathService pathService, LoaderService loaderService, CountryRepository countryRepository, FlightRepository flightRepository, FlightSearchRepository flightSearchRepository, LocationCodeMapper locationCodeMapper, AirportConnectionFinder airportConnectionFinder, TequilaClient tequilaClient) {
        this.pathService = pathService;
        this.loaderService = loaderService;
        this.countryRepository = countryRepository;
        this.flightRepository = flightRepository;
        this.flightSearchRepository = flightSearchRepository;
        this.locationCodeMapper = locationCodeMapper;
        this.airportConnectionFinder = airportConnectionFinder;
        this.tequilaClient = tequilaClient;
    }

    public List<FlightView> searchItineraries(Location from,
                                              Location to,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              Boolean roundTrip,
                                              boolean returnKiwiLinks,
                                              @Nullable Integer minLayover,
                                              @Nullable Integer maxLayover,
                                              @Nullable Integer hopPenalty,
                                              @Nullable Integer layoverPenaltyPerHour,
                                              @Nullable Integer shortLayoverPenalty,
                                              @Nullable Integer flightTimePenaltyPerHour
    ) {
        String fromCountry = getCountryFromLocation(from);
        String toCountry = getCountryFromLocation(to);
        CostWeights weights = createWeights(hopPenalty, layoverPenaltyPerHour, shortLayoverPenalty, flightTimePenaltyPerHour);

        log.info("Finding paths between {} and {} for dates from {} to {}", fromCountry, toCountry, fromDate, toDate);
        Set<String> countryCodes = getRelevantCountryCodes(fromCountry, toCountry, fromDate, toDate, roundTrip);
        log.info("Loading country data for countries {}", countryCodes);
        List<Country> countries =
                countryRepository.findByIdIn(countryCodes.stream().toList())
                        .stream()
                        .map(as(Country.class))
                        .toList();
        Instant loadStart = Instant.now();
        log.info("Loading flights for countries {} for dates from {} to {}",
                countryCodes, fromDate, toDate);
        Set<String> airports = countries
                .stream()
                .flatMap(country -> country.getAirports().stream())
                .map(Airport::getId)
                .collect(Collectors.toSet());
        List<Flight> flights = flightSearchRepository.findSearchFlights(
                airports.stream().toList(),
                fromDate.toString(),
                toDate.toString());
        log.info("Loaded {} flights to search in {} ms", flights.size(), Duration.between(loadStart, Instant.now()).toMillis());
        Instant searchStart = Instant.now();
        List<Flight> shortestPath;
        if (roundTrip) {
            log.info("Searching for shortest round trip between {} and {}", from, to);
            shortestPath =
                    new DijkstraSearch(flights, locationCodeMapper, airportConnectionFinder, minLayover, maxLayover)
                            .findShortestPath(from, from, List.of(to), weights);
            log.info("Found shortest round trip between {} and {}", from, to);
        } else {
            log.info("Searching for shortest one way trip between {} and {}", from, to);
            shortestPath =
                    new DijkstraSearch(flights, locationCodeMapper, airportConnectionFinder, minLayover, maxLayover)
                            .findShortestPath(from, to, List.of(), weights);
            log.info("Found shortest one way trip between {} and {}", from, to);
        }
        log.info("Finished search in {} ms", Duration.between(searchStart, Instant.now()).toMillis());
        return convertToFlightViewList(shortestPath, countries, returnKiwiLinks);
    }

    private static CostWeights createWeights(Integer hopPenalty, Integer layoverPenaltyPerHour, Integer shortLayoverPenalty, Integer flightTimePenaltyPerHour) {
        return new CostWeights(
                hopPenalty != null ? hopPenalty : DEFAULT_HOP_PENALTY,
                layoverPenaltyPerHour != null ? layoverPenaltyPerHour : DEFAULT_LAYOVER_PENALTY_PER_HOUR,
                shortLayoverPenalty != null ? shortLayoverPenalty : DEFAULT_SHORT_LAYOVER_PENALTY,
                flightTimePenaltyPerHour != null ? flightTimePenaltyPerHour : DEFAULT_FLIGHT_TIME_PENALTY_PER_HOUR
        );
    }

    private List<FlightView> convertToFlightViewList(List<Flight> shortestPath, List<Country> countries, boolean returnKiwiLinks) {
        return shortestPath.stream()
                .map(f -> convertToFlightView(f, countries, returnKiwiLinks))
                .toList();
    }

    private FlightView convertToFlightView(Flight flight, List<Country> countries, boolean returnKiwiLinks) {
        FlightView flightView = copyFieldsIgnoreUnknown(FlightView.class, flight);
        Airport fromAirport = getAirport(countries, flight.getFromAirport());
        if (fromAirport.getTimezone() != null) {
            flightView.setFromTime(getLocalDateTime(flight.getFromTime(), fromAirport));
        }
        Airport toAirport = getAirport(countries, flight.getToAirport());
        if (toAirport.getTimezone() != null) {
            flightView.setToTime(getLocalDateTime(flight.getToTime(), toAirport));
        }
        flightView.setDuration(Duration.between(flight.getFromTime(), flight.getToTime()));
        if (returnKiwiLinks) {
            flightView.setKiwiLink(loadKiwiLink(flight));
        }
        flightView.setFromAirportMunicipality(fromAirport.getMunicipality());
        flightView.setToAirportMunicipality(toAirport.getMunicipality());
        return flightView;
    }

    private Airport getAirport(List<Country> countries, String airportId) {
        return countries.stream()
                .flatMap(c -> c.getAirports().stream())
                .filter(a -> airportId.equals(a.getId()))
                .findFirst()
                .get();
    }

    private LocalDateTime getLocalDateTime(LocalDateTime utcTime, Airport airport) {
        return utcTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of(airport.getTimezone()))
                .toLocalDateTime();
    }

    private String getCountryFromLocation(Location location) {
        return location.isCountry() ?
                location.getCountryCode() :
                countryRepository.findCountryByAirport(location.getAirportCodes().getFirst()).getId();
    }

    private Set<String> getRelevantCountryCodes(String from, String to, LocalDate fromDate, LocalDate toDate, Boolean roundTrip) {
        List<ShortestPath> paths = new ArrayList<>();
        YearMonth current = YearMonth.from(fromDate);
        YearMonth toMonth = YearMonth.from(toDate);
        while (!current.isAfter(toMonth)) {
            paths.addAll(pathService.findShortestPathsFromTo(from, to, current));
            if (roundTrip) {
                paths.addAll(pathService.findShortestPathsFromTo(to, from, current));
            }
            current = current.plusMonths(1);
        }
        return paths.stream().flatMap(path -> path.route().stream()).collect(Collectors.toSet());
    }

    private String loadKiwiLink(Flight flight) {
        List<Flight> foundFlights = tequilaClient.search(
                flight.getFromAirport(),
                flight.getToAirport(),
                null,
                flight.getFromTime().toLocalDate(),
                flight.getToTime().toLocalDate(),
                null);
        loaderService.saveFlights(foundFlights);
        for (Flight foundFlight : foundFlights) {
            if (foundFlight.getFromTime().equals(flight.getFromTime()) &&
                    foundFlight.getToTime().equals(flight.getToTime())) {
                return foundFlight.getKiwiLink();
            }
        }
        flightRepository.delete(flight);
        return null;
    }
}
