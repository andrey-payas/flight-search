package com.paias.air.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {
    @GetMapping("/search-ui")
    public String searchPage() {
        return "search-ui.html";
    }

    @GetMapping("/heuristics-ui")
    public String heuristicsPage() {
        return "heuristics-ui.html";
    }

    @GetMapping("/paths-ui")
    public String pathsPage() {
        return "paths-ui.html";
    }
}
