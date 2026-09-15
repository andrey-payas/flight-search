package com.paias.air.controller;

import com.paias.air.model.controller.FlightView;
import com.paias.air.search.Location;
import com.paias.air.service.ItinerarySearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class SearchController {
    private final ItinerarySearchService itinerarySearchService;

    public SearchController(ItinerarySearchService itinerarySearchService) {
        this.itinerarySearchService = itinerarySearchService;
    }

    @GetMapping("search-itineraries")
    @ResponseBody
    public List<FlightView> searchItineraries(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("from_date") LocalDate fromDate,
            @RequestParam("to_date") LocalDate toDate,
            @RequestParam(value = "return_kiwi_links", required = false) Boolean returnKiwiLinks,
            @RequestParam(value = "roundtrip", required = false) Boolean roundtrip,
            @RequestParam(value = "min_layover", required = false) Integer minLayover,
            @RequestParam(value = "max_layover", required = false) Integer maxLayover,
            @RequestParam(value = "hop_penalty", required = false) BigDecimal hopPenalty,
            @RequestParam(value = "layover_penalty", required = false) BigDecimal layoverPenaltyPerHour,
            @RequestParam(value = "flight_time_penalty", required = false) BigDecimal flightTimePenaltyPerHour
    ) {
        return itinerarySearchService.searchItineraries(
                new Location(from),
                new Location(to),
                fromDate,
                toDate,
                roundtrip,
                returnKiwiLinks != null && returnKiwiLinks,
                minLayover,
                maxLayover,
                toCents(hopPenalty),
                toCents(layoverPenaltyPerHour),
                null,
                toCents(flightTimePenaltyPerHour)
        );
    }

    private Integer toCents(BigDecimal amount) {
        return amount != null ? amount.multiply(new BigDecimal(100)).intValue() : null;
    }
}
