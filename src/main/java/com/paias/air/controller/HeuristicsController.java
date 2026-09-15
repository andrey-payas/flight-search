package com.paias.air.controller;

import com.paias.air.model.n4j.projection.CountryShortestPath;
import com.paias.air.model.n4j.projection.ShortestPath;
import com.paias.air.service.HeuristicsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@Controller
public class HeuristicsController {
    private final HeuristicsService heuristicsService;

    public HeuristicsController(HeuristicsService heuristicsService) {
        this.heuristicsService = heuristicsService;
    }

    @GetMapping(value = "heuristics/months")
    @ResponseBody
    public List<String> getMonthsWithHeuristics() {
        return heuristicsService.getMonthsWithHeuristics();
    }
}
