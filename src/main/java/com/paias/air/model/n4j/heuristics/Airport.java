package com.paias.air.model.n4j.heuristics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@Node(value = "Airport")
public class Airport {
    @Id
    private String id;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String timezone;
    private String municipality;
    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.OUTGOING)
    private Country country;

    public Airport(String id) {
        this.id = id;
    }

    public Airport() {
    }

    @Override
    public String toString() {
        return "Airport{" +
                "id='" + id + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", timezone='" + timezone + '\'' +
                ", municipality='" + municipality + '\'' +
                ", country=" + country +
                '}';
    }
}
