package com.paias.air.service;

import com.paias.air.model.n4j.heuristics.Country;
import com.paias.air.model.n4j.heuristics.CountryRoute;
import com.paias.air.model.n4j.projection.ShortestPath;
import com.paias.air.model.n4j.search.SearchRecord;
import com.paias.air.repository.CountryRepository;
import com.paias.air.repository.CountryRouteRepository;
import com.paias.air.repository.PathService;
import com.paias.air.repository.SearchRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import static com.paias.air.util.ProjectionUtils.as;

@Slf4j
@Service
public class SearchPreparationService {
    private static final int MAX_PRICE_LOWER_LIMIT = 100;
    private static final int PRICE_SLACK_FACTOR = 2;
    private static final String ONE_PER_CITY_SEARCH_TYPE = "one_per_city";
    private static final String DIRECT_SEARCH_TYPE = "direct";

    private final PathService pathService;
    private final CountryRouteRepository countryRouteRepository;
    private final LoaderService loaderService;
    private final SearchRecordRepository searchRecordRepository;
    private final CountryRepository countryRepository;

    public SearchPreparationService(PathService pathService, CountryRouteRepository countryRouteRepository, LoaderService loaderService, SearchRecordRepository searchRecordRepository, CountryRepository countryRepository) {
        this.pathService = pathService;
        this.countryRouteRepository = countryRouteRepository;
        this.loaderService = loaderService;
        this.searchRecordRepository = searchRecordRepository;
        this.countryRepository = countryRepository;
    }

    public void searchOutgoingForEachCountry(YearMonth month, boolean allCountries) {
        List<Country> countries = getCountries(allCountries);
        for (Country country : countries) {
            LocalDate dateFrom = month.atDay(1);
            String from = country.getId();
            log.info("Loading outgoing flights for country {} at {}", from, month);
            List<SearchRecord> records = searchRecordRepository.findByFromAndDateFromAndSearchType(from,
                    dateFrom, ONE_PER_CITY_SEARCH_TYPE);
            if (records.isEmpty()) {
                loaderService.loadOutgoingForCountry(month, country, allCountries);
                searchRecordRepository.save(
                        new SearchRecord(from, null, dateFrom, dateFrom.plusMonths(1),
                                LocalDateTime.now(), ONE_PER_CITY_SEARCH_TYPE));
            }
        }
    }

    private List<Country> getCountries(boolean allCountries) {
        List<Country> countries;
        if (allCountries) {
            countries = countryRepository.findAllIdProjectionsBy()
                    .stream()
                    .map(as(Country.class))
                    .toList();
        } else {
            countries = loaderService.getTopCountries().stream()
                    .map(Country::new)
                    .collect(Collectors.toList());
        }
        return countries;
    }

    public void loadFlightsForRoute(String flyFrom, String flyTo, YearMonth month, int budget) {
        var route = countryRouteRepository.findCountryRouteProjectionById(CountryRoute.getId(flyFrom, flyTo, month));
        if (route.isPresent() && route.get().getPrice() != null) {
            Long maxPrice = Math.max(Math.min(route.get().getPrice() * PRICE_SLACK_FACTOR, budget), MAX_PRICE_LOWER_LIMIT);
            loaderService.loadForMonth(flyFrom, flyTo, month, maxPrice);
        } else {
            loaderService.loadForMonth(flyFrom, flyTo, month, (long) budget);
        }
    }

    public void loadFlightsForPaths(String from, String to, YearMonth month, int budget) {
        List<ShortestPath> paths = pathService.findShortestPathsFromTo(from, to, month);
        for (ShortestPath path : paths) {
            loadFlightsForPaths(month, path, budget);
        }
    }

    private void loadFlightsForPaths(YearMonth month, ShortestPath path, int budget) {
        log.info("Checking path {}", String.join(",", path.route()));
        for (int i = 0; i < path.route().size() - 1; i++) {
            String from = path.route().get(i);
            String to = path.route().get(i + 1);
            String destination = path.route().getLast();
            loadFlightsForRoute(month, budget, from, to);
            loadFlightsForRoute(month, budget, from, destination);
        }
    }

    private void loadFlightsForRoute(YearMonth month, int budget, String from, String to) {
        LocalDate dateFrom = month.atDay(1);
        LocalDate dateTo = month.plusMonths(1).atDay(1);
        List<SearchRecord> records = searchRecordRepository.findByFromAndToAndDateFromAndSearchType(
                from, to, dateFrom, DIRECT_SEARCH_TYPE);
        if (records.isEmpty()) {
            loadFlightsForRoute(from, to, month, budget);
            searchRecordRepository.save(new SearchRecord(from, to, dateFrom,
                    dateTo, LocalDateTime.now(), DIRECT_SEARCH_TYPE));
        }
    }
}
