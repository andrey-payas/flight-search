package com.paias.air.search;

import com.paias.air.model.controller.CostWeights;
import com.paias.air.model.n4j.Flight;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DijkstraSearch {
    public static final int SEARCH_WINDOW_MONTHS = 36;
    public static final int DEFAULT_DESTINATION_STAY = 10 * 86400;
    public static final int MAX_LAYOVER = 6 * 86400;
    public static final int MIN_LAYOVER = 4 * 3600;
    private static final Comparator<State> BY_COST =
            (a, b) -> Integer.compare(a.cost, b.cost);
    private final List<SFlight>[] flightsByAirport;
    private final LocationCodeMapper locationCodeMapper;
    private final AirportConnectionFinder airportConnectionFinder;
    private final PriorityQueue<State> queue = new PriorityQueue<>(BY_COST);
    private final Map<StateKey, Integer> bestCost;
    private final Map<StateKey, StateKey> parent = new HashMap<>();
    private final Map<StateKey, SFlight> parentFlight = new HashMap<>();
    private final List<Flight> shortestPath = new ArrayList<>();
    private boolean found = false;
    private final Set<Integer> toAirports = new HashSet<>();
    private final List<Set<Integer>> intermediaryDestinations = new ArrayList<>();
    private final List<Integer> intermediaryCountryIds = new ArrayList<>();
    private final StateKey tempKey = new StateKey(0, 0, 0);
    private int edgeCount = 0;

    private CostCalculator costCalculator;
    private final int minLayover;
    private final int maxLayover;
    private final int destinationStay;

    public DijkstraSearch(List<Flight> flights, LocationCodeMapper locationCodeMapper,
                          AirportConnectionFinder airportConnectionFinder, Integer minLayover, Integer maxLayover, Integer destinationStay) {
        this.locationCodeMapper = locationCodeMapper;
        this.flightsByAirport = flightsByDepartureAirport(flights);
        this.airportConnectionFinder = airportConnectionFinder;
        this.minLayover = minLayover != null ? minLayover * 3600 : MIN_LAYOVER;
        this.maxLayover = maxLayover != null ? maxLayover * 3600 : MAX_LAYOVER;
        this.destinationStay = destinationStay != null ? Math.max(0, destinationStay * 86400 - this.minLayover) : DEFAULT_DESTINATION_STAY;
        bestCost = new HashMap<>(flights.size() * 2);
    }

    public List<Flight> findShortestPath(Location origin, Location destination,
                                         List<Location> intermediaryDestinationCountries, CostWeights weights) {
        resetSearchState();
        costCalculator = new CostCalculator(weights);
        initialize(origin, destination, intermediaryDestinationCountries);
        return findShortestPath();
    }

    private void initialize(Location origin, Location destination, List<Location> intermediaryDestinations) {

        this.toAirports.addAll(getAirportIds(destination));
        for (Location location : intermediaryDestinations) {
            if (location.isCountry()) {
                int countryId = locationCodeMapper.getLocationId(location.getCountryCode());
                Set<Integer> airportsInCountry = airportConnectionFinder.getConnections(countryId).stream()
                        .map(Connection::getAirport)
                        .collect(Collectors.toSet());
                this.intermediaryDestinations.add(airportsInCountry);
                this.intermediaryCountryIds.add(countryId);
            } else {
                this.intermediaryDestinations.add(getAirportIds(location));
                this.intermediaryCountryIds.add(null);
            }
        }

        initializeFirstNode(origin);
    }

    private Set<Integer> getAirportIds(Location location) {
        if (!location.isCountry()) {
            return location.getAirportCodes()
                    .stream()
                    .map(locationCodeMapper::getLocationId)
                    .collect(Collectors.toSet());
        } else {
            int destinationCountryId = locationCodeMapper.getLocationId(location.getCountryCode());
            return airportConnectionFinder.getConnections(destinationCountryId).stream()
                    .map(Connection::getAirport)
                    .collect(Collectors.toSet());
        }
    }

    private void initializeFirstNode(Location origin) {
        if (origin.isCountry()) {
            int originCountryId = locationCodeMapper.getLocationId(origin.getCountryCode());
            int nowEpoch = getEpochSecond(LocalDateTime.now());
            addFirstNode(originCountryId, nowEpoch);
        } else {
            Set<Integer> airportIds = getAirportIds(origin);
            int nowEpoch = getEpochSecond(LocalDateTime.now());
            for (Integer airportId : airportIds) {
                addFirstNode(airportId, nowEpoch);
            }
        }
    }

    private void addFirstNode(Integer locationId, int time) {
        StateKey startKey = new StateKey(locationId, time, 0);
        queue.add(new State(startKey, 0));
        bestCost.put(startKey, 0);
    }

    private List<Flight> findShortestPath() {
        while (!queue.isEmpty() && !found) {
            processNode();
        }
        log.info("Processed {} edges of a graph", edgeCount);
        return shortestPath;
    }

    private void processNode() {
        State state = queue.poll();
        if (state == null) {
            return;
        }
        int toTime;
        if (state.cost == 0 && state.key.phase == 0) {
            toTime = state.key.time + (SEARCH_WINDOW_MONTHS * 30 * 86400);
        } else {
            toTime = state.key.time + maxLayover;
        }

        if (state.cost != bestCost.get(state.key)) {
            return;
        }

        if (isIntermediaryGoal(state)) {
            advancePhase(state);
            return;
        }

        if (isGoal(state.key)) {
            found = true;
            reconstructPath(state.key);
            return;
        }
        int fromTime = state.key.time;
        processFlights(state.key.locationId, state.key.time, toTime, state);
        List<Connection> connections = airportConnectionFinder.getConnections(state.key.locationId);
        for (Connection connection : connections) {
            processFlights(connection.getAirport(),
                    fromTime + connection.getCost(), toTime, state);
        }
    }

    private void advancePhase(State state) {
        Integer countryId = intermediaryCountryIds.get(state.key.phase);
        if (countryId != null) {
            updateIfBetter(state, countryId, state.key.time + destinationStay, state.key.phase + 1, state.cost, null);
        } else {
            for (Integer airportId : intermediaryDestinations.get(state.key.phase)) {
                updateIfBetter(state, airportId, state.key.time + destinationStay, state.key.phase + 1, state.cost, null);
            }
        }
    }

    private boolean isIntermediaryGoal(State state) {
        return state.key.phase < intermediaryDestinations.size() &&
                intermediaryDestinations.get(state.key.phase).contains(state.key.locationId);
    }

    private void processFlights(int airport, int fromTime, int toTime,
                                State state) {
        List<SFlight> flights = flightsByAirport[airport];
        int i = getEarliestFlight(flights, fromTime + minLayover);
        if (i == -1) {
            return;
        }
        SFlight flight = flights.get(i);
        while (flight.fromTime < toTime) {
            edgeCount++;
            int newCost = costCalculator.calculate(state, flight);

            updateIfBetter(state, flight.toAirport, flight.toTime, state.key.phase, newCost, flight);
            i++;
            if (i == flights.size()) {
                break;
            }
            flight = flights.get(i);
        }
    }

    private void updateIfBetter(State state, int locationId, int time, int phase, int newCost, SFlight flight) {
        tempKey.locationId = locationId;
        tempKey.time = time;
        tempKey.phase = phase;
        Integer cost = bestCost.get(tempKey);
        if (cost == null || cost > newCost) {
            StateKey newKey = new StateKey(locationId, time, phase);
            bestCost.put(newKey, newCost);
            parent.put(newKey, state.key);
            parentFlight.put(newKey, flight);
            queue.add(new State(newKey, newCost));
        }
    }

    private void reconstructPath(StateKey key) {
        while (parent.containsKey(key)) {
            SFlight flight = parentFlight.get(key);
            if (flight != null) {
                shortestPath.add(flight.flight);
            }
            key = parent.get(key);
        }
        Collections.reverse(shortestPath);
    }

    private boolean isGoal(StateKey key) {
        return key.phase == intermediaryDestinations.size() && toAirports.contains(key.locationId);
    }

    private int getEarliestFlight(List<SFlight> flights, int fromTime) {
        if (flights == null) {
            return -1;
        }
        int left = 0;
        int right = flights.size() - 1;
        int resultIndex = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            SFlight flight = flights.get(mid);

            if (flight.fromTime >= fromTime) {
                resultIndex = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return resultIndex;
    }

    private List<SFlight>[] flightsByDepartureAirport(List<Flight> flights) {
        List<SFlight>[] flightsByAirport = new List[locationCodeMapper.getTotalLocations()];

        for (Flight flight : flights) {
            String airport = flight.getFromAirport();
            Integer airportId = locationCodeMapper.getLocationId(airport);
            if (flightsByAirport[airportId] == null) {
                flightsByAirport[airportId] = new ArrayList<>();
            }
            flightsByAirport[airportId].add(convertFlight(flight));
        }

        for (List<SFlight> flightList : flightsByAirport) {
            if (flightList != null) {
                flightList.sort(Comparator.comparing(SFlight::getFromTime));
            }
        }
        return flightsByAirport;
    }

    private SFlight convertFlight(Flight flight) {
        int fromAirportId = locationCodeMapper.getLocationId(flight.getFromAirport());
        int toAirportId = locationCodeMapper.getLocationId(flight.getToAirport());
        int fromTime = getEpochSecond(flight.getFromTime());
        int toTime = getEpochSecond(flight.getToTime());
        int price = flight.getPrice().intValue() * 100;

        return new SFlight(flight, fromAirportId, fromTime, toAirportId, toTime, price, flight.getLegCount());
    }

    private static int getEpochSecond(LocalDateTime time) {
        return (int) time.toEpochSecond(ZoneOffset.UTC);
    }

    private void resetSearchState() {
        queue.clear();
        bestCost.clear();
        parent.clear();
        parentFlight.clear();
        shortestPath.clear();
        toAirports.clear();
        intermediaryDestinations.clear();
        intermediaryCountryIds.clear();
        found = false;
        edgeCount = 0;
    }
}
