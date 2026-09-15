package com.paias.air.search;

import java.util.Objects;

public class StateKey {
    int locationId;
    int time;
    int phase;

    public StateKey(int locationId, int time, int phase) {
        this.locationId = locationId;
        this.time = time;
        this.phase = phase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof StateKey stateKey) {
            return locationId == stateKey.locationId && time == stateKey.time && phase == stateKey.phase;
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * (31 * locationId + time) + phase;
    }

    @Override
    public String toString() {
        return "StateKey{" +
                "airport=" + locationId +
                ", time=" + time +
                ", phase=" + phase +
                '}';
    }
}
