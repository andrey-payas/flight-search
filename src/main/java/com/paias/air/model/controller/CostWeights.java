package com.paias.air.model.controller;

public record CostWeights(int hopPenalty,
                          int layoverPenaltyPerHour,
                          int shortLayoverPenalty,
                          int flightTimePenaltyPerHour) {
}
