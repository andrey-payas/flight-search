package com.paias.air.model.n4j.search;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Node(value = "SearchRecord")
public class SearchRecord {
    @Id
    @GeneratedValue
    private Long id;

    private String from;
    private String to;

    private LocalDate dateFrom;
    private LocalDate dateTo;

    private LocalDateTime queriedAt;
    private String searchType;
    public SearchRecord() {
    }

    public SearchRecord(String from, String to, LocalDate dateFrom, LocalDate dateTo,
                        LocalDateTime queriedAt, String searchType) {
        this.from = from;
        this.to = to;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.queriedAt = queriedAt;
        this.searchType = searchType;
    }
}
