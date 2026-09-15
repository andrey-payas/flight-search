package com.paias.air.repository;

import com.paias.air.model.n4j.heuristics.Airport;
import com.paias.air.model.n4j.projection.IdProjection;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportRepository extends Neo4jRepository<Airport, String> {
    List<IdProjection> findAllIdProjectionsBy();
}
