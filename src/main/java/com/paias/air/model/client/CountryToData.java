package com.paias.air.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CountryToData {
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
}
