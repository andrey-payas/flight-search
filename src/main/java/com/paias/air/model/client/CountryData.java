package com.paias.air.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CountryData {
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
}
