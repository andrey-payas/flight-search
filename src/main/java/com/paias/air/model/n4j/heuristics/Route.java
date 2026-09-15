package com.paias.air.model.n4j.heuristics;

import com.paias.air.model.n4j.Flight;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
@Node(value = "Route")
public class Route {
    @Id
    private String id;
    private int confidence;
    private YearMonth month;
    @Relationship(type = "FROM", direction = Relationship.Direction.OUTGOING, cascadeUpdates = false)
    private Airport from;
    @Relationship(type = "TO", direction = Relationship.Direction.OUTGOING, cascadeUpdates = false)
    private Airport to;
    @Transient
    private List<Flight> cheapestFlights;

    public Route() {
    }

    public Route(int confidence, YearMonth month, Airport from, Airport to) {
        this.confidence = confidence;
        this.month = month;
        this.from = from;
        this.to = to;
        this.id = getId(from.getId(), to.getId(), month);
        this.cheapestFlights = Collections.emptyList();
    }

    public static String getId(String from, String to, YearMonth month) {
        return from + "_" + to + "_" + month;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(id, route.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

