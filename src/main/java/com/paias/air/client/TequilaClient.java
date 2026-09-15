package com.paias.air.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paias.air.converter.FlightConverter;
import com.paias.air.model.client.SearchResponse;
import com.paias.air.model.n4j.Flight;
import com.paias.air.model.n4j.SearchSnapshot;
import com.paias.air.repository.SearchSnapshotRepositoryProxy;
import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TequilaClient {

    private final RestTemplate restTemplate;
    private final FlightConverter flightConverter;

    @Value("${tequila.apikey}")
    private String apiKey;
    @Value("${tequila.url}")
    private String url;
    private final SearchSnapshotRepositoryProxy snapshotRepository;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final BlockingQueue<SearchRequest> queue = new LinkedBlockingQueue<>();
    private static final DateTimeFormatter DD_MM_YYYY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TequilaClient(RestTemplate restTemplate, SearchSnapshotRepositoryProxy snapshotRepository, ObjectMapper objectMapper,
                         @Value("${tequila.delay-between-requests}") int delayBetweenRequests, FlightConverter flightConverter) {
        this.restTemplate = restTemplate;
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = objectMapper;
        this.executorService.scheduleWithFixedDelay(this::handleRequest, 0, delayBetweenRequests, TimeUnit.MILLISECONDS);
        this.flightConverter = flightConverter;
    }

    public void handleRequest() {
        SearchRequest request;
        try {
            request = queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SearchResponse response;
        try {
            response = search(request.query());
        } catch (Exception e) {
            request.response.completeExceptionally(e);
            return;
        }
        request.response().complete(response);
    }

    record SearchRequest(String query, CompletableFuture<SearchResponse> response) {
    }

    @SneakyThrows
    public List<Flight> search(String flyFrom, YearMonth month, boolean oneForCity) {
        String dateFrom = firstDay(month);
        String dateTo = firstDay(month.plusMonths(1));
        String oneForCityParam = oneForCity ? "1" : "0";
        String queryParams = String.format("fly_from=%s&date_from=%s&date_to=%s&one_for_city=%s", flyFrom, dateFrom, dateTo, oneForCityParam);
        return searchFlightsByQuery(queryParams);
    }

    @SneakyThrows
    public List<Flight> search(String flyFrom, String flyTo, Long priceTo, YearMonth month, boolean oneForCity) {
        String dateFrom = firstDay(month);
        String dateTo = firstDay(month.plusMonths(1));
        String oneForCityParam = oneForCity ? "1" : "0";
        String queryParams = String.format("fly_from=%s&fly_to=%s&date_from=%s&date_to=%s&one_for_city=%s", flyFrom, flyTo, dateFrom, dateTo, oneForCityParam);
        String transientParams = String.format("limit=200&sort=price&price_to=%s", priceTo);
        return searchFlightsByQuery(queryParams, transientParams);
    }

    @SneakyThrows
    public List<Flight> search(String flyFrom, String flyTo, Long priceTo, YearMonth month) {
        String dateFrom = firstDay(month);
        String dateTo = firstDay(month.plusMonths(1));
        String queryParams = String.format("fly_from=%s&fly_to=%s&date_from=%s&date_to=%s", flyFrom, flyTo, dateFrom, dateTo);
        String transientParams = String.format("limit=200&sort=price&price_to=%s", priceTo);
        return searchFlightsByQuery(queryParams, transientParams);
    }

    @SneakyThrows
    public List<Flight> search(String flyFrom, String dateFrom, String dateTo) {
        String queryParams = String.format("fly_from=%s&date_from=%s&date_to=%s", flyFrom, dateFrom, dateTo);
        return searchFlightsByQuery(queryParams);
    }

    @SneakyThrows
    public List<Flight> searchForMonth(String flyFrom, String flyTo, YearMonth month) {
        String dateFrom = firstDay(month);
        String dateTo = firstDay(month.plusMonths(1));
        String queryParams = String.format("fly_from=%s&fly_to=%s&date_from=%s&date_to=%s", flyFrom, flyTo, dateFrom, dateTo);
        return searchFlightsByQuery(queryParams);
    }

    @SneakyThrows
    public List<Flight> search(
            @Nullable String flyFrom,
            @Nullable String flyTo,
            @Nullable Long priceTo,
            @Nullable LocalDate dateFrom,
            @Nullable LocalDate dateTo,
            @Nullable Boolean oneForCity) {
        return search(flyFrom, flyTo, priceTo, formatDate(dateFrom), formatDate(dateTo), oneForCity);
    }

    @SneakyThrows
    public List<Flight> search(
            @Nullable String flyFrom,
            @Nullable String flyTo,
            @Nullable Long priceTo,
            @Nullable String dateFrom,
            @Nullable String dateTo,
            @Nullable Boolean oneForCity) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("fly_from", flyFrom);
        paramMap.put("fly_to", flyTo);
        paramMap.put("price_to", priceTo);
        paramMap.put("date_from", dateFrom);
        paramMap.put("date_to", dateTo);
        paramMap.put("one_for_city", oneForCity);
        List<String> queryParamsList = paramMap.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .sorted()
                .collect(Collectors.toList());
        String queryParams = String.join("&", queryParamsList);
        return searchFlightsByQuery(queryParams);
    }

    private List<Flight> searchFlightsByQuery(String queryParams) throws JsonProcessingException {
        return searchFlightsByQuery(queryParams, "");
    }

    private List<Flight> searchFlightsByQuery(String queryParams, String transientParams) throws JsonProcessingException {
        return searchByQuery(queryParams, transientParams)
                .getFlights()
                .stream()
                .map(flightConverter::flightDataToFlight)
                .toList();
    }

    private SearchResponse searchByQuery(String queryParams, String transientParams) throws JsonProcessingException {
        SearchSnapshot snapshot = snapshotRepository.findByQuery(queryParams);
        if (snapshot != null) {
            return objectMapper.readValue(snapshot.getJson(), SearchResponse.class);
        }

        String fullQuery = queryParams;
        if (transientParams != null && !transientParams.isEmpty()) {
            fullQuery += "&" + transientParams;
        }

        SearchRequest request = new SearchRequest(fullQuery, new CompletableFuture<>());
        queue.add(request);
        CompletableFuture<SearchResponse> futureResponse = request.response();
        try {
            SearchResponse response = futureResponse.get();
            snapshotRepository.save(new SearchSnapshot(queryParams, objectMapper.writeValueAsString(response)));
            return response;
        } catch (Exception ex) {
            log.error("Error executing search for query '{}'", request.query(), ex);
            return getEmptyResponse();
        }
    }

    private static @NonNull SearchResponse getEmptyResponse() {
        SearchResponse emptyResponse = new SearchResponse();
        emptyResponse.setFlights(List.of());
        return emptyResponse;
    }

    private SearchResponse search(String queryParams) throws JsonProcessingException {
        log.info("Searching for query '{}'", queryParams);
        String json = restTemplate.exchange(
                url + "/v2/search?" + queryParams,
                HttpMethod.GET,
                entity(),
                String.class
        ).getBody();
        return objectMapper.readValue(json, SearchResponse.class);
    }

    private HttpEntity<Object> entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        return new HttpEntity<>(headers);
    }

    private static @NonNull String firstDay(YearMonth month) {
        return month.atDay(1)
                .format(DD_MM_YYYY);
    }

    private String formatDate(LocalDate date) {
        return DD_MM_YYYY.format(date);
    }
}
