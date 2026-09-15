package com.paias.air.search;

import com.paias.air.model.n4j.heuristics.Airport;
import com.paias.air.model.n4j.heuristics.Country;
import com.paias.air.repository.AirportRepository;
import com.paias.air.repository.CountryRepository;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paias.air.util.ProjectionUtils.as;

@Component
public class LocationCodeMapper {
    private final Map<String, Integer> locationCodeToId;
    private final String[] idToLocationCode;
    private final int airportCount;

    public LocationCodeMapper(AirportRepository airportRepository, CountryRepository countryRepository) {
        List<Airport> airports = airportRepository.findAllIdProjectionsBy()
                .stream()
                .map(as(Airport.class))
                .toList();
        List<Country> countries = countryRepository.findAllCountryProjectionsBy()
                .stream()
                .map(as(Country.class))
                .toList();
        this.locationCodeToId = new HashMap<>();
        airportCount = airports.size();
        this.idToLocationCode = new String[airportCount + countries.size()];

        for (int i = 0; i < airportCount; i++) {
            Airport airport = airports.get(i);
            String code = airport.getId();
            locationCodeToId.put(code, i);
            idToLocationCode[i] = code;
        }
        for (int i = airportCount; i < airportCount + countries.size(); i++) {
            String code = countries.get(i - airportCount).getId();
            locationCodeToId.put(code, i);
            idToLocationCode[i] = code;
        }
    }

    public Integer getLocationId(String code) {
        return locationCodeToId.get(code);
    }

    public String getAirportCode(int id) {
        return idToLocationCode[id];
    }

    public int getTotalLocations() {
        return idToLocationCode.length;
    }
    public boolean isCountry(int id){
        return id >= airportCount;
    }

}
