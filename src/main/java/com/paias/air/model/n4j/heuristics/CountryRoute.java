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

@Data
@Node("CountryRoute")
public class CountryRoute {
    @Id
    private String id;
    private YearMonth month;
    private int confidence;
    @Relationship(type = "FROM", direction = Relationship.Direction.OUTGOING)
    private Country from;
    @Relationship(type = "TO", direction = Relationship.Direction.OUTGOING)
    private Country to;
    private Long price;
    @Transient
    private List<Flight> cheapestFlights;

    public CountryRoute() {
    }

    public CountryRoute(int confidence, YearMonth month, Country from, Country to) {
        this.month = month;
        this.confidence = confidence;
        this.from = from;
        this.to = to;
        this.id = getId(from.getId(), to.getId(), month);
        this.cheapestFlights = Collections.emptyList();
    }

    public static String getId(String from, String to, YearMonth month) {
        return from + "_" + to + "_" + month;
    }
}
