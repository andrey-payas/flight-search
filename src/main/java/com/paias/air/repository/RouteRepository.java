package com.paias.air.repository;

import com.paias.air.model.n4j.heuristics.Route;
import com.paias.air.model.n4j.projection.RouteRow;
import com.paias.air.model.n4j.projection.RouteProjection;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface RouteRepository extends Neo4jRepository<Route, String> {
    @Query("""
            MATCH (r:Route {id: $rId})
            OPTIONAL MATCH (r)-[rel:INCLUDES]->()
            DELETE rel
            WITH r
            UNWIND $flights AS flight
            MERGE (f:Flight {id: flight.id})
            MERGE (r)-[:INCLUDES]->(f)
            """)
    void saveCheapestFlights(@Param("rId") String rId, @Param("flights") List<Map<String, Object>> flights);

    @Query("""
    UNWIND $routes AS routeData

    MATCH (r:Route {id: routeData.id})
    OPTIONAL MATCH (r)-[rel:INCLUDES]->()
    DELETE rel

    WITH r, routeData
    UNWIND routeData.flights AS flight

    MERGE (f:Flight {id: flight.id})
    MERGE (r)-[:INCLUDES]->(f)
""")
    void saveCheapestFlightsForRoutes(
            @Param("routes") List<Map<String, Object>> routes
    );

    @Query("""
            MATCH (c:Route)
            -[:FROM]->(fromAirport:Airport)
            -[:LOCATED_IN]->(fromCountry:Country {id: $fromId})
            MATCH (c)-[toRel:TO]->(toAirport:Airport)
            -[:LOCATED_IN]->(toCountry:Country)
            WHERE c.month = $month
            RETURN c            AS route,
                                                                  fromAirport  AS fromAirport,
                                                                  toAirport    AS toAirport,
                                                                  toCountry    AS toCountry
            """)
    List<RouteRow> findAllFromCountry(@Param("fromId") String fromId, @Param("month") String month);

    Optional<RouteProjection> findRouteProjectionById(String id);
}
