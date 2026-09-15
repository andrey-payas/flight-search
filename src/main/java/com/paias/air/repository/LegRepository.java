package com.paias.air.repository;

import com.paias.air.model.n4j.Flight;
import com.paias.air.model.n4j.Leg;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegRepository extends Neo4jRepository<Leg, String> {
}
