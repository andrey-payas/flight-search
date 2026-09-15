package com.paias.air.model.n4j;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Node("Flight")
public class Flight {
    @Id
    String id;

    private String fromAirport;
    private LocalDateTime fromTime;
    private String toAirport;
    private LocalDateTime toTime;
    private Long price;
    private int legCount;

    @Relationship(type = "HAS_LEG", direction = Relationship.Direction.OUTGOING)
    private List<Leg> legs;

    @Transient
    private String toCountryCode;
    @Transient
    private String kiwiLink;
    public Flight() {
    }

    public Flight(String id, String fromAirport, LocalDateTime fromTime, String toAirport, LocalDateTime toTime, Long price, List<Leg> legs, String toCountryCode, String kiwiLink, int legCount) {
        this.id = id;
        this.fromAirport = fromAirport;
        this.fromTime = fromTime;
        this.toAirport = toAirport;
        this.toTime = toTime;
        this.price = price;
        this.legs = legs;
        this.toCountryCode = toCountryCode;
        this.kiwiLink = kiwiLink;
        this.legCount = legCount;
    }
}