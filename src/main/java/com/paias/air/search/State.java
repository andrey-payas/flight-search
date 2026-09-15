package com.paias.air.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class State {
    final StateKey key;
    final int cost;

    public State(StateKey key, int cost) {
        this.key = key;
        this.cost = cost;
    }
}
