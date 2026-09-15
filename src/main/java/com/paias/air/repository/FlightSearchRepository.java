package com.paias.air.repository;

import com.paias.air.model.n4j.Flight;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class FlightSearchRepository {
    private final Neo4jClient neo4jClient;

    public FlightSearchRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public List<Flight> findSearchFlights(
            List<String> airportIds,
            String dateFrom,
            String dateTo) {

        return neo4jClient.query("""
                        MATCH (f:Flight)
                        WHERE f.fromAirport IN $airportIds
                          AND f.toAirport IN $airportIds
                          AND f.toTime >= localdatetime($dateFrom)
                          AND f.toTime <= localdatetime($dateTo)
                        RETURN f.fromAirport AS fromAirport,
                               f.toAirport AS toAirport,
                               f.fromTime AS fromTime,
                               f.toTime AS toTime,
                               f.price AS price,
                               f.id AS id,
                               f.legCount AS legCount
                        """)
                .bind(airportIds).to("airportIds")
                .bind(dateFrom).to("dateFrom")
                .bind(dateTo).to("dateTo")
                .fetch()
                .all()
                .stream()
                .map(this::toFlight)
                .toList();
    }

    private Flight toFlight(Map<String, Object> row) {
        return new Flight(
                (String) row.get("id"),
                (String) row.get("fromAirport"),
                (LocalDateTime) row.get("fromTime"),
                (String) row.get("toAirport"),
                (LocalDateTime) row.get("toTime"),
                ((Number) row.get("price")).longValue(),
                null,
                null,
                null,
                ((Number) row.get("legCount")).intValue()
        );
    }
}
