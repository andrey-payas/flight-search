package com.paias.air.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LegData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("combination_id")
    private String combinationId;
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
    @JsonProperty("airline")
    private String airline;
    @JsonProperty("flight_no")
    private Long flightNo;
    @JsonProperty("operating_carrier")
    private String operatingCarrier;
    @JsonProperty("operating_flight_no")
    private String operatingFlightNo;
    @JsonProperty("fare_basis")
    private String fareBasis;
    @JsonProperty("fare_category")
    private String fareCategory;
    @JsonProperty("fare_classes")
    private String fareClasses;
    @JsonProperty("return")
    private Boolean returns;
    @JsonProperty("bags_recheck_required")
    private Boolean bagsRecheckRequired;
    @JsonProperty("vi_connection")
    private Boolean viConnection;
    @JsonProperty("guarantee")
    private Boolean guarantee;
    @JsonProperty("vehicle_type")
    private String vehicleType;
}
