package com.paias.air.search;

import com.paias.air.model.n4j.heuristics.Airport;
import com.paias.air.model.n4j.heuristics.Country;
import com.paias.air.repository.AirportRepository;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

@Component
public class AirportLoader {
    public static final Set<String> AIRPORT_TYPES = Set.of("large_airport", "medium_airport", "small_airport");
    @Value("${airport.csv.data}")
    private String csvPath;
    @Value("${airport.csv.data.timezone}")
    private String timezoneCsvPath;
    @Value("${airport.csv.loadonstartup}")
    private boolean loadOnStartup;
    private static final Collection<String> COUNTRY_EXCEPTIONS = Set.of("AQ","EH","IO","UM","RU","BY","");
    private final AirportRepository repository;

    public AirportLoader(AirportRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        if (loadOnStartup) {
            Collection<Airport> airports = load();
            for (Airport airport : airports) {
                if (!COUNTRY_EXCEPTIONS.contains(airport.getCountry().getId())) {
                    repository.save(airport);
                }
            }
        }
    }

    @SneakyThrows
    public Collection<Airport> load() {
        Map<String, Airport> airports = new HashMap<>();
        doForEachRecord(csvPath, record -> addAirport(record, airports));
        doForEachRecord(timezoneCsvPath, record-> updateTimezoneAndMunicipality(record, airports));
        return airports.values();
    }

    private void doForEachRecord(String csvPath, Consumer<CSVRecord> consumer) throws IOException {
        try (Reader reader = Files.newBufferedReader(Path.of(csvPath));
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .get()
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                consumer.accept(record);
            }
        }
    }

    private void updateTimezoneAndMunicipality(CSVRecord record, Map<String, Airport> airports) {
        Airport airport = airports.get(record.get("iata"));
        String timezone = record.get("timezone");
        if(timezone != null && !timezone.isBlank() && airport!=null){
            airport.setTimezone(timezone);
        }

        String municipality = record.get("municipality");
        if(municipality != null && !municipality.isBlank() && airport!=null){
            airport.setMunicipality(municipality);
        }
    }

    private static void addAirport(CSVRecord record, Map<String,Airport> airports) {
        String airportType = record.get("type");
        String iataCode = record.get("iata_code");
        if (!AIRPORT_TYPES.contains(airportType)||
                (iataCode == null || iataCode.isBlank())) {
            return;
        }

        airports.put(iataCode, getAirport(record));
    }

    private static Airport getAirport(CSVRecord record) {
        Airport airport = new Airport();
        String coordinates = record.get("coordinates");
        if(coordinates!=null && !coordinates.isBlank()){
            String[] split = coordinates.split(",");
            String latitude = split[0].strip();
            String longitude = split[1].strip();
            airport.setLatitude(new BigDecimal(latitude));
            airport.setLongitude(new BigDecimal(longitude));
        }
        String municipality = record.get("municipality");
        if(municipality !=null && !municipality.isBlank()) {
            airport.setMunicipality(municipality);
        }

        airport.setId(record.get("iata_code"));
        airport.setCountry(new Country(record.get("iso_country")));

        return airport;
    }
}
