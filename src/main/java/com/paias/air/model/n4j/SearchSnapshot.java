package com.paias.air.model.n4j;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node("SearchSnapshot")
public class SearchSnapshot {
    @Id
    private String query;
    private String json;

    public SearchSnapshot() {
    }

    public SearchSnapshot(String query, String json) {
        this.query = query;
        this.json = json;
    }
}
