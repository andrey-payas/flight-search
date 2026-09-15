package com.paias.air.model.controller;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class FlightView{
    private String id;
    private String fromAirport;
    private LocalDateTime fromTime;
    private String toAirport;
    private LocalDateTime toTime;
    private Long price;
    private Duration duration;
    private String kiwiLink;
    private String fromAirportMunicipality;
    private String toAirportMunicipality;
}
