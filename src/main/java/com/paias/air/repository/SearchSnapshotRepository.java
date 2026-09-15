package com.paias.air.repository;

import com.paias.air.model.n4j.SearchSnapshot;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchSnapshotRepository extends Neo4jRepository<SearchSnapshot, String> {
    SearchSnapshot findByQuery(String queryParams);
}
