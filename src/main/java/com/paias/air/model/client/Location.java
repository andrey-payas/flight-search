package com.paias.air.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Location {
    private String code;
    @JsonProperty("int_id")
    private Long id;

    public Location() {
    }

    public Location(String code, Long id) {
        this.code = code;
        this.id = id;
    }
}
