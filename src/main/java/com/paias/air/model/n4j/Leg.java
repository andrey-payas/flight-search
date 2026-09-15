package com.paias.air.model.n4j;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Data
@Node("Leg")
public class Leg {
    @Id
    private String id;
    private String flyFrom;
    private String flyTo;
    private LocalDateTime utcDeparture;
    private LocalDateTime utcArrival;
    private Long flightNo;
}
