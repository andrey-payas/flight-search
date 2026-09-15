package com.paias.air.service;

import com.paias.air.repository.PathService;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class SearchPreparationFacade {

    private final HeuristicsService heuristicsService;
    private final SearchPreparationService searchPreparationService;
    private final PathService pathService;

    public SearchPreparationFacade(HeuristicsService heuristicsService, SearchPreparationService searchPreparationService, PathService pathService) {
        this.heuristicsService = heuristicsService;
        this.searchPreparationService = searchPreparationService;
        this.pathService = pathService;
    }

    public void prepareHeuristics(YearMonth start, YearMonth end, boolean projectionOnly, boolean allCountries) {
        start = firstValidMonth(start);
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            if (!projectionOnly) {
                searchPreparationService.searchOutgoingForEachCountry(month, allCountries);
                heuristicsService.aggregateHeuristicsForEachCountry(month);
            }
            pathService.projectCountryCostGraph(month);
        }
    }

    public void preparePaths(String from, String to, int budget, boolean projectionOnly, YearMonth start, YearMonth end) {
        start = firstValidMonth(start);
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            pathService.projectPathSearch(from, month);
            pathService.projectPathSearch(to, month);
            if (!projectionOnly) {
                searchPreparationService.loadFlightsForPaths(from, to, month, budget);
                searchPreparationService.loadFlightsForPaths(to, from, month, budget);
            }
        }
    }

    private static YearMonth firstValidMonth(YearMonth month) {
        YearMonth firstValidMonth = YearMonth.now(ZoneOffset.UTC).plusMonths(1);
        return !month.isBefore(firstValidMonth) ? month : firstValidMonth;
    }
}
