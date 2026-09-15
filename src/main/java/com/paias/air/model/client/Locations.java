package com.paias.air.model.client;

import lombok.Data;

import java.util.List;

@Data
public class Locations {
    List<Location> locations;

    public Locations(List<Location> locations) {
        this.locations = locations;
    }

    public Locations() {
    }
}
