package com.paias.air.search;

import lombok.Getter;

@Getter
public class Connection {
    private final int airport;
    //Time cost in seconds
    private final int cost;

    public Connection(int airport, int cost) {
        this.airport = airport;
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "airport=" + airport +
                ", cost=" + cost +
                '}';
    }
}
