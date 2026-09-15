package com.paias.air.repository;

import com.paias.air.model.n4j.search.PathsSnapshot;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;

@Repository
public interface PathsSnapshotRepository extends Neo4jRepository<PathsSnapshot, Long> {
    PathsSnapshot findByFromAndToAndMonth(
            String from,
            String to,
            YearMonth month
    );
}
