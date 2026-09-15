package com.paias.air.model.n4j.search;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.YearMonth;

@Getter
@Setter
@Node(value = "PathsSnapshot")
public class PathsSnapshot {

    @Id
    @GeneratedValue
    private Long id;
    private String from;
    private String to;
    private YearMonth month;
    private String paths;

    public PathsSnapshot(String from, String to, YearMonth month, String paths) {
        this.from = from;
        this.to = to;
        this.month = month;
        this.paths = paths;
    }

    public PathsSnapshot() {
    }
}
