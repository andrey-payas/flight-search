package com.paias.air.model.n4j.heuristics;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@Node("Country")
public class Country {
    @Id
    private String id;
    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.INCOMING)
    private List<Airport> airports;

    public Country() {
    }

    public Country(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Country{" +
                "id='" + id + '\'' +
                ", airports=" + airports +
                '}';
    }
}
