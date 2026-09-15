package com.paias.air.controller;

import com.paias.air.service.SearchPreparationFacade;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.YearMonth;

@Controller
public class PreparationController {
    private final SearchPreparationFacade searchPreparationFacade;

    public PreparationController(SearchPreparationFacade searchPreparationFacade) {
        this.searchPreparationFacade = searchPreparationFacade;
    }

    @PostMapping("prepare-heuristics")
    @ResponseBody
    public void prepareHeuristics(@RequestParam("fromDate") YearMonth fromDate, @RequestParam("toDate") YearMonth toDate,
                                                    @RequestParam(value = "projectionOnly", required = false) boolean projectionOnly,
                                                    @RequestParam(value = "allCountries", required = false) boolean allCountries) {
        searchPreparationFacade.prepareHeuristics(fromDate, toDate, projectionOnly, allCountries);
    }

    @GetMapping("prepare-paths")
    @ResponseBody
    public void preparePaths(
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("budget") int budget,
            @RequestParam(value = "projectionOnly", required = false) boolean projectionOnly
    ) {
        searchPreparationFacade.preparePaths(from, to, budget, projectionOnly, YearMonth.parse(fromDate), YearMonth.parse(toDate));
    }
}
