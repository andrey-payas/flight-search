package com.paias.air.search;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class Location {
    private static final String AIRPORT_CODE_REGEX = "[A-Z]{3}";
    private static final String AIRPORT_LIST_SEPARATOR = ",";

    @Getter
    private final boolean isCountry;
    private List<String> airportCodes;
    private String countryCode;

    public Location(String location) {
        if(isAirportList(location)){
            airportCodes = getAirportCodes(location);
            isCountry = false;
        }else {
            countryCode = location;
            isCountry = true;
        }
    }

    private boolean isAirportList(String destination) {
        List<String> locations = getAirportCodes(destination);
        return locations
                .stream()
                .allMatch(location -> location.matches(AIRPORT_CODE_REGEX));
    }

    private List<String> getAirportCodes(String destination) {
        return Arrays.stream(destination.split(AIRPORT_LIST_SEPARATOR))
                .toList();
    }

    public List<String> getAirportCodes() {
        if (isCountry) {
            throw new IllegalStateException("Location is a country");
        }
        return airportCodes;
    }

    public String getCountryCode() {
        if (!isCountry) {
            throw new IllegalStateException("Location is not a country");
        }
        return countryCode;
    }
}
