package com.paias.air.repository;

import com.paias.air.model.n4j.Flight;
import com.paias.air.model.n4j.projection.CheapestFlightsResult;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightRepository extends Neo4jRepository<Flight, String> {
    @Query("""
            MATCH (r:Route {id: $id})-[:INCLUDES]->(f)
            RETURN f
            """)
    List<Flight> findCheapestFlights(@Param("id") String id);
    @Query("""
            MATCH (cr:CountryRoute {id: $id})-[:INCLUDES]->(f)
            RETURN f
            """)
    List<Flight> findCheapestFlightsForCountry(@Param("id") String id);

    @Query("""
            MATCH (r:Route)
            WHERE r.id IN $ids
            OPTIONAL MATCH (r)-[:INCLUDES]->(f)
            RETURN r.id AS id, collect(f) AS flights
""")
    List<CheapestFlightsResult> findCheapestFlights(@Param("ids") List<String> ids);

    @Query("""
    MATCH (f:Flight)
    WHERE f.fromAirport IN $airportsIds
      AND f.toAirport IN $airportsIds
      AND f.toTime >= localdatetime($dateFrom)
      AND f.toTime <= localdatetime($dateTo)
    RETURN f
""")
    List<Flight> findFlightsForAirportsBy(@Param("airportsIds") List<String> airportsIds,
                                                    @Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo);
}
