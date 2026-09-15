package com.paias.air.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlightData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("flyFrom")
    private String flyFrom;
    @JsonProperty("flyTo")
    private String flyTo;
    @JsonProperty("cityFrom")
    private String cityFrom;
    @JsonProperty("cityCodeFrom")
    private String cityCodeFrom;
    @JsonProperty("cityTo")
    private String cityTo;
    @JsonProperty("cityCodeTo")
    private String cityCodeTo;


    @JsonProperty("local_departure")
    private LocalDateTime localDeparture;
    @JsonProperty("utc_departure")
    private LocalDateTime utcDeparture;
    @JsonProperty("local_arrival")
    private LocalDateTime localArrival;
    @JsonProperty("utc_arrival")
    private LocalDateTime utcArrival;

    @JsonProperty("quality")
    private BigDecimal quality;
    @JsonProperty("distance")
    private BigDecimal distance;

    @JsonProperty("price")
    private Long price;

    @JsonProperty("countryTo")
    private CountryToData countryTo;

    @JsonProperty("deep_link")
    private String deepLink;

    @JsonProperty("route")
    private List<LegData> route;
}
