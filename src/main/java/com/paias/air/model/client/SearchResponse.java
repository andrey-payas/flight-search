package com.paias.air.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    @JsonProperty("search_id")
    private String searchId;
    @JsonProperty("data")
    private List<FlightData> flights;
}
