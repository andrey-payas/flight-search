package com.paias.air.controller;

import com.paias.air.service.LoaderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.YearMonth;

@Controller
public class LoadController {
    private final LoaderService loaderService;

    public LoadController(LoaderService loaderService) {
        this.loaderService = loaderService;
    }

    @PostMapping(value = "load", params = {"fly_from", "date_from", "date_to"})
    @ResponseBody
    public void load(@RequestParam("fly_from") String fly_from, @RequestParam("date_from") String date_from,
                     @RequestParam("date_to") String date_to) {
        loaderService.load(fly_from, date_from, date_to);
    }

    @PostMapping(value = "load", params = {"fly_from", "fly_to", "date"})
    @ResponseBody
    public void loadRouteForMonth(@RequestParam("fly_from") String fly_from, @RequestParam("fly_to") String fly_to,
                                  @RequestParam("date") String date_to) {
        loaderService.loadForMonth(fly_from, fly_to, YearMonth.parse(date_to));
    }
}
