package com.paias.air.repository;

import com.paias.air.model.n4j.Flight;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class FlightSearchRepository {
    private final Neo4jClient neo4jClient;
    private final Driver driver;
    @org.springframework.beans.factory.annotation.Value("${neo4j.dbname}")
    private String dbName;
    public FlightSearchRepository(Neo4jClient neo4jClient, Driver driver) {
        this.neo4jClient = neo4jClient;
        this.driver = driver;
    }

    public List<Flight> findSearchFlights(
            List<Map<String, Object>> countryRoutes,
            String dateFrom,
            String dateTo) {

        String query = """
            UNWIND $countryRoutes AS route
            MATCH (f:Flight {
                fromCountry: route.from,
                toCountry: route.to
            })
            WHERE f.toTime >= localdatetime($dateFrom)
              AND f.toTime <= localdatetime($dateTo)
            RETURN route, collect([
                   f.id,
                   f.fromAirport,
                   f.toAirport,
                   f.fromTime,
                   f.toTime,
                   f.price,
                   f.legCount]) AS flights;
            """;

        Map<String, Object> params = Map.of(
                "countryRoutes", countryRoutes,
                "dateFrom", dateFrom,
                "dateTo", dateTo
        );

        try (Session session = driver.session(
                SessionConfig.forDatabase(dbName))) {

            return session.executeRead(tx -> {
                var result = tx.run(query, params);

                List<Flight> flights = new ArrayList<>();

                while (result.hasNext()) {
                    Record record = result.next();
                    for (Value value : record.get("flights").values()) {
                        List<Value> fields = new ArrayList<>();
                        value.values().forEach(fields::add);

                        Flight flight = new Flight();
                        flight.setId(fields.get(0).asString());
                        flight.setFromAirport(fields.get(1).asString());
                        flight.setToAirport(fields.get(2).asString());
                        flight.setFromTime(fields.get(3).asLocalDateTime());
                        flight.setToTime(fields.get(4).asLocalDateTime());
                        flight.setPrice(fields.get(5).asLong());
                        flight.setLegCount(fields.get(6).asInt());

                        flights.add(flight);
                    }
                }

                return flights;
            });
        }
    }
}
