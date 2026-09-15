package com.paias.air.repository;

import com.paias.air.model.n4j.heuristics.CountryRoute;
import com.paias.air.model.n4j.projection.CountryRoutesInMonthCount;
import com.paias.air.model.n4j.projection.CountryRouteProjection;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CountryRouteRepository extends Neo4jRepository<CountryRoute, String> {
    @Query("""
            MATCH (cr:CountryRoute {id: $crId})
            OPTIONAL MATCH (cr)-[rel:INCLUDES]->()
            DELETE rel
            WITH cr
            UNWIND $flights AS flight
            MERGE (f:Flight {id: flight.id})
            MERGE (cr)-[:INCLUDES]->(f)
            """)
    void saveCheapestFlights(@Param("crId") String crId, @Param("flights") List<Map<String, Object>> flights);
    Optional<CountryRouteProjection> findCountryRouteProjectionById(String id);


    @Query("""
            MATCH (cr:CountryRoute)
            RETURN cr.month AS month, count(cr) AS count
            ORDER BY cr.month
            """)
    List<CountryRoutesInMonthCount> countByMonthBy();
}
