package com.paias.air.search;

import com.paias.air.model.controller.CostWeights;

public class CostCalculator {

    private final int hopPenalty;
    private final int layoverPenaltyPerHour;
    private final int shortLayoverPenalty;
    private final int flightTimePenaltyPerHour;

    public CostCalculator(CostWeights weights) {
        hopPenalty = weights.hopPenalty();
        layoverPenaltyPerHour = weights.layoverPenaltyPerHour();
        shortLayoverPenalty = weights.shortLayoverPenalty();
        flightTimePenaltyPerHour = weights.flightTimePenaltyPerHour();
    }

    public int calculate(State state, SFlight nextFlight) {
        int cost = state.cost;
        cost += nextFlight.price;
        cost += nextFlight.hops * hopPenalty;

        int flightSeconds = nextFlight.toTime - nextFlight.fromTime;
        cost += (flightSeconds / 3600) * flightTimePenaltyPerHour;

        if (isFirstFlight(state)) {
            return cost;
        }

        int layoverSeconds = nextFlight.fromTime - state.key.time;
        cost += (layoverSeconds / 3600) * layoverPenaltyPerHour;
        if (4 * 3600 <= layoverSeconds && layoverSeconds <= 24 * 3600) {
            cost += shortLayoverPenalty;
        }
        return cost;
    }

    private boolean isFirstFlight(State state) {
        return state.key.phase == 0 && state.cost == 0;
    }
}
