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

import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class AirportLoader {
    @Value("${airport.csv.data}")
    private String csvPath;
    @Value("${airport.csv.loadonstartup}")
    private boolean loadOnStartup;
    private static final Collection<String> countryExceptions= Arrays.asList("AQ","EH","IO","UM","RU","BY","");
    private final AirportRepository repository;

    public AirportLoader(AirportRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        if (loadOnStartup) {
            List<Airport> airports = load();
            for (Airport airport : airports) {
                if (!countryExceptions.contains(airport.getCountry().getId())) {
                    repository.save(airport);
                }
            }
        }
    }

    @SneakyThrows
    public List<Airport> load() {
        Path path = Path.of(csvPath);
        List<Airport> airports = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .get()
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                String airportType = record.get("airport_type");
                if (!List.of("large_airport", "medium_airport").contains(airportType)) {
                    continue;
                }

                airports.add(getAirport(record));
            }
        }
        return airports;
    }

    private static Airport getAirport(CSVRecord record) {
        Airport airport = new Airport();
        String latitude = record.get("latitude");
        if(latitude!=null && !latitude.isBlank()){
            airport.setLatitude(new BigDecimal(latitude));
        }
        String longitude = record.get("longitude");
        if(longitude !=null && !longitude.isBlank()) {
            airport.setLongitude(new BigDecimal(longitude));
        }

        String municipality = record.get("municipality");
        if(municipality !=null && !municipality.isBlank()) {
            airport.setMunicipality(municipality);
        }

        airport.setId(Optional
                .of(record.get("iata"))
                .filter(iata -> !iata.isEmpty())
                .orElse(record.get("ident")));
        airport.setCountry(new Country(record.get("country")));

        String timezone = record.get("timezone");
        if (timezone != null && !timezone.isBlank()) {
            airport.setTimezone(timezone);
        }
        return airport;
    }
}
