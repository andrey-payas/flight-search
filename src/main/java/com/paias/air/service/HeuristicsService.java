package com.paias.air.service;

import com.paias.air.model.n4j.Flight;
import com.paias.air.model.n4j.heuristics.*;
import com.paias.air.model.n4j.projection.*;
import com.paias.air.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.paias.air.util.ProjectionUtils.as;

@Slf4j
@Service
public class HeuristicsService {
    private static final int CHEAP_FLIGHT_SIZE = 10;
    private static final Comparator<Flight> BY_PRICE_THEN_ID =
            Comparator.comparing(HeuristicsService::getPrice)
                    .thenComparing(Flight::getId);
    private static final int MINIMUM_EXPECTED_COUNTRY_ROUTES = 1000;

    private final RouteRepository routeRepository;
    private final CountryRouteRepository countryRouteRepository;
    private final CountryRepository countryRepository;
    private final FlightRepository flightRepository;
    private final Neo4jTemplate neo4jTemplate;

    public HeuristicsService(RouteRepository routeRepository, CountryRouteRepository countryRouteRepository, CountryRepository countryRepository, FlightRepository flightRepository, Neo4jOperations neo4jTemplate) {
        this.routeRepository = routeRepository;
        this.countryRouteRepository = countryRouteRepository;
        this.countryRepository = countryRepository;
        this.flightRepository = flightRepository;
        this.neo4jTemplate = (Neo4jTemplate) neo4jTemplate;
    }

    public void aggregateHeuristicsForEachCountry(YearMonth month) {
        List<Country> countries = findAllCountries();
        for (Country country : countries) {
            aggregateHeuristics(country, month);
        }
    }

    public List<String> getMonthsWithHeuristics() {
        return countryRouteRepository.countByMonthBy()
                .stream()
                .filter(p -> p.count() > MINIMUM_EXPECTED_COUNTRY_ROUTES)
                .map(p -> p.month().toString())
                .collect(Collectors.toList());
    }

    private void aggregateHeuristics(Country country, YearMonth month) {
        String from = country.getId();
        Map<String, List<Route>> groupedByDestination = findFromCountryGroupedByDestinationCountry(from, month);
        populateCheapFlights(groupedByDestination.values().stream().flatMap(Collection::stream).toList());
        updateCountryRoute(groupedByDestination, from, month);
        log.info("Country {} has been populated", country.getId());
    }

    private @NonNull Map<String, List<Route>> findFromCountryGroupedByDestinationCountry(String from, YearMonth month) {
        return findAndGroupByKey(month, from, row -> row.toCountry().getId());
    }

    private @NonNull Map<String, List<Route>> findFromCountryGroupedByDestinationAirport(String from, YearMonth month) {
        return findAndGroupByKey(month, from, row -> row.toAirport().getId());
    }

    private @NonNull Map<String, List<Route>> findAndGroupByKey(YearMonth month, String from, Function<RouteRow, String> keyExtractor) {
        return routeRepository
                .findAllFromCountry(from, month.toString())
                .stream().map(
                        row ->
                                new AbstractMap.SimpleEntry<>(keyExtractor.apply(row), convertToRoute(row))).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private List<Country> findAllCountries() {
        return countryRepository.findAllIdProjectionsBy()
                .stream()
                .map(as(Country.class))
                .toList();
    }

    private void populateCheapFlights(List<Route> allFromCountry) {
        Map<String, List<Flight>> flightsById = flightRepository.findCheapestFlights(
                        allFromCountry.stream()
                                .map(Route::getId)
                                .toList()).stream()
                .collect(Collectors.toMap(
                        CheapestFlightsResult::id,
                        CheapestFlightsResult::flights
                ));
        allFromCountry.forEach(route -> route.setCheapestFlights(flightsById.get(route.getId())));
    }

    private Route convertToRoute(RouteRow row) {
        Route c = row.route();
        c.setFrom(new Airport(row.fromAirport().getId()));
        c.setTo(new Airport(row.toAirport().getId()));
        return c;
    }

    private void updateCountryRoute(Map<String, List<Route>> groupedByDestination, String from, YearMonth month) {
        groupedByDestination.forEach((to, routes) -> {
            CountryRoute countryRoute = findCountryRouteOrCreate(from, to, month);
            List<Flight> cheapestFlights = routes.stream().flatMap(route -> route.getCheapestFlights().stream())
                    .sorted(BY_PRICE_THEN_ID).limit(CHEAP_FLIGHT_SIZE).toList();
            updateCountryRoute(countryRoute, cheapestFlights);
        });
    }

    public void aggregateFlightsFromCountry(List<Flight> flights, String countryCode, YearMonth month) {
        Map<String, List<Route>> groupedByDestination = findFromCountryGroupedByDestinationAirport(countryCode, month);
        Set<Route> updatedRoutes = new HashSet<>();
        List<Route> createdRoutes = new ArrayList<>();
        populateCheapFlights(groupedByDestination.values().stream().flatMap(Collection::stream).toList());
        for (Flight flight : flights) {
            groupedByDestination.putIfAbsent(flight.getToAirport(), List.of());
            List<Route> routes = groupedByDestination.get(flight.getToAirport());
            Route route = findRoute(flight, routes)
                    .orElse(findRoute(flight, createdRoutes)
                            .orElse(null));
            if (route == null) {
                route = createRoute(flight.getFromAirport(), flight.getToAirport(), month);
                createdRoutes.add(route);
            }

            if (updateRoute(route, flight.getId(), flight.getPrice())) {
                updatedRoutes.add(route);
            }
        }

        neo4jTemplate.saveAllAs(createdRoutes, RouteProjection.class);
        updateRoutes(updatedRoutes);
    }

    private Optional<Route> findRoute(Flight flight, List<Route> routes) {
        return routes.stream()
                .filter(r -> r.getFrom().getId().equals(flight.getFromAirport()) &&
                        r.getTo().getId().equals(flight.getToAirport()))
                .findFirst();
    }

    public void aggregateFlights(List<Flight> flights) {
        Map<RouteKey, List<Flight>> grouped = flights.stream()
                .collect(Collectors.groupingBy(flight -> new RouteKey(
                        flight.getFromAirport(),
                        flight.getToAirport(),
                        YearMonth.from(flight.getFromTime())
                )));
        for (Map.Entry<RouteKey, List<Flight>> entry : grouped.entrySet()) {
            RouteKey key = entry.getKey();
            Route route = findRouteOrCreate(key.flyFrom(), key.flyTo(), key.month());
            boolean changed = false;
            for (Flight flight : entry.getValue()) {
                changed |= updateRoute(route, flight.getId(), flight.getPrice());
            }
            if (changed) {
                saveRoute(route);
            }
        }
    }

    private Route createRoute(String flyFrom, String flyTo, YearMonth month) {
        Airport from = new Airport();
        from.setId(flyFrom);
        Airport to = new Airport();
        to.setId(flyTo);
        return new Route(0, month, from, to);
    }

    private CountryRoute createCountryRoute(String flyFrom, String flyTo, YearMonth month) {
        Country from = new Country();
        from.setId(flyFrom);
        Country to = new Country();
        to.setId(flyTo);
        return new CountryRoute(0, month, from, to);
    }

    private boolean updateRoute(Route route, String flightId, Long flightPrice) {
        TreeSet<Flight> cheapFlights =
                new TreeSet<>(BY_PRICE_THEN_ID);
        cheapFlights.addAll(route.getCheapestFlights());

        Flight candidate = new Flight();
        candidate.setId(flightId);
        candidate.setPrice(flightPrice);

        cheapFlights.removeIf(f -> f.getId().equals(flightId));
        cheapFlights.add(candidate);
        while (cheapFlights.size() > CHEAP_FLIGHT_SIZE) {
            cheapFlights.pollLast();
        }

        if (cheapFlights.contains(candidate)) {
            route.setCheapestFlights(new ArrayList<>(cheapFlights));
            return true;
        }
        return false;
    }

    private void updateCountryRoute(CountryRoute countryRoute, List<Flight> flights) {
        countryRoute.setPrice(flights.getFirst().getPrice());
        neo4jTemplate.saveAs(countryRoute, CountryRouteProjection.class);
        countryRouteRepository.saveCheapestFlights(countryRoute.getId(), getCheapestFlightsMap(flights));
    }

    private Route findRouteOrCreate(String flyFrom, String flyTo, YearMonth month) {
        String id = Route.getId(flyFrom, flyTo, month);
        return findRoute(id)
                .orElseGet(() -> createRoute(flyFrom, flyTo, month));
    }

    private Optional<Route> findRoute(String id) {
        return routeRepository.findRouteProjectionById(id)
                .map(as(Route.class))
                .map(this::loadCheapestFlights);
    }

    private Route loadCheapestFlights(Route route) {
        route.setCheapestFlights(
                flightRepository.findCheapestFlights(route.getId())
        );
        return route;
    }

    private void saveRoute(Route route) {
        neo4jTemplate.saveAs(route, RouteProjection.class);
        routeRepository.saveCheapestFlights(route.getId(), getCheapestFlightsMap(route.getCheapestFlights()));
    }

    private void updateRoutes(Collection<Route> routes) {
        List<Map<String, Object>> routesParam = routes.stream()
                .map(route -> Map.of(
                        "id", route.getId(),
                        "flights", route.getCheapestFlights().stream()
                                .map(f -> Map.of("id", f.getId()))
                                .toList()
                ))
                .toList();
        routeRepository.saveCheapestFlightsForRoutes(routesParam);
    }

    private CountryRoute findCountryRouteOrCreate(String flyFrom, String flyTo, YearMonth month) {
        String id = CountryRoute.getId(flyFrom, flyTo, month);
        return countryRouteRepository.findCountryRouteProjectionById(id)
                .map(as(CountryRoute.class))
                .orElseGet(() -> createCountryRoute(flyFrom, flyTo, month));
    }

    private List<Map<String, Object>> getCheapestFlightsMap(List<Flight> cheapestFlights) {
        return cheapestFlights.stream()
                .map(f -> Map.of("id", (Object) f.getId())).toList();
    }
    private static Long getPrice(Flight f) {
        return f.getPrice();
    }


    private record RouteKey(
            String flyFrom,
            String flyTo,
            YearMonth month
    ) {}
}
