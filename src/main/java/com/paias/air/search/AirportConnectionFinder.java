package com.paias.air.search;

import com.paias.air.model.n4j.heuristics.Airport;
import com.paias.air.model.n4j.heuristics.Country;
import com.paias.air.repository.CountryRepository;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.paias.air.util.ProjectionUtils.as;

@Component
public class AirportConnectionFinder {

    public static final int ONE_DAY = 24 * 60 * 60;
    public static final double SHORT_DISTANCE = 200.0;
    public static final double EARTH_RADIUS = 6371.0;
    private final CountryRepository countryRepository;
    private final LocationCodeMapper locationCodeMapper;

    @Getter
    private final List<Connection>[] connectionsArray;

    @SuppressWarnings("unchecked")
    public AirportConnectionFinder(CountryRepository countryRepository, LocationCodeMapper locationCodeMapper) {
        this.countryRepository = countryRepository;
        this.locationCodeMapper = locationCodeMapper;
        int totalLocations = locationCodeMapper.getTotalLocations();
        this.connectionsArray = new List[totalLocations];
        for (int i = 0; i < totalLocations; i++) {
            this.connectionsArray[i] = new ArrayList<>();
        }
        initialize();
    }

    public List<Connection> getConnections(int locationId) {
        return connectionsArray[locationId];
    }

    private void initialize() {
        List<Country> countries = countryRepository.findAllCountriesWithAirportsBy()
                .stream()
                .map(as(Country.class))
                .toList();

        for (Country country : countries) {
            List<Airport> airports = country.getAirports();
            if (airports == null) {
                continue;
            }

            for (int i = 0; i < airports.size(); i++) {
                for (int j = i + 1; j < airports.size(); j++) {
                    Airport airport1 = airports.get(i);
                    Airport airport2 = airports.get(j);
                    if(airport1.getLatitude() == null || airport1.getLongitude() == null || airport2.getLatitude() == null || airport2.getLongitude() == null) {
                        continue;
                    }
                    double distance = calculateDistance(airport1, airport2);

                    if (distance < SHORT_DISTANCE) {
                        int airportId1 = locationCodeMapper.getLocationId(airport1.getId());
                        int airportId2 = locationCodeMapper.getLocationId(airport2.getId());
                        addConnection(airportId1, airportId2);
                        addConnection(airportId2, airportId1);
                    }
                }
            }
        }

        for (Country country : countries) {
            Integer countryId = locationCodeMapper.getLocationId(country.getId());
            for (Airport airport : country.getAirports()) {
                addCountryConnection(countryId, locationCodeMapper.getLocationId(airport.getId()));
            }
        }
    }

    private void addCountryConnection(int countryId, int toAirport) {
        connectionsArray[countryId].add(new Connection(toAirport, 0));
    }

    private void addConnection(int fromAirport, int toAirport) {
        connectionsArray[fromAirport].add(new Connection(toAirport, ONE_DAY));
    }

    private double calculateDistance(Airport airport1, Airport airport2) {
        double lat1Rad = Math.toRadians(airport1.getLatitude().doubleValue());
        double lon1Rad = Math.toRadians(airport1.getLongitude().doubleValue());
        double lat2Rad = Math.toRadians(airport2.getLatitude().doubleValue());
        double lon2Rad = Math.toRadians(airport2.getLongitude().doubleValue());

        double x = (lon2Rad - lon1Rad) * Math.cos((lat1Rad + lat2Rad) / 2);
        double y = (lat2Rad - lat1Rad);

        return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
    }
}
