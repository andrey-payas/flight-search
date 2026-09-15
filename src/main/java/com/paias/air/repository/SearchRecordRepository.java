package com.paias.air.repository;

import com.paias.air.model.n4j.search.SearchRecord;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SearchRecordRepository extends Neo4jRepository<SearchRecord, Long> {
    List<SearchRecord> findByFromAndToAndDateFromAndSearchType(
            String from,
            String to,
            LocalDate dateFrom,
            String searchType
    );
    List<SearchRecord> findByFromAndDateFromAndSearchType(
            String from,
            LocalDate dateFrom,
            String searchType
    );
}
