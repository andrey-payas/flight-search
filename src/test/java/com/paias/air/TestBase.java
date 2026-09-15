package com.paias.air;

import com.paias.air.config.AppConfig;
import com.paias.air.config.AppConfig;
import com.paias.air.config.Neo4jConfig;
import com.paias.air.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestAppConfig.class, Neo4jConfig.class})
@TestPropertySource("classpath:test.properties")
public class TestBase {
    @Autowired
    FlightRepository flightRepository;
    @Autowired
    LegRepository legRepository;
    @Autowired
    RouteRepository routeRepository;
    @Autowired
    CountryRouteRepository countryRouteRepository;
    @Autowired
     SearchRecordRepository searchRecordRepository;
    @Autowired
    private SearchSnapshotRepository searchSnapshotRepository;

    @AfterEach
    public void tearDown() {
        flightRepository.deleteAll();
        legRepository.deleteAll();
        searchSnapshotRepository.deleteAll();
        routeRepository.deleteAll();
        countryRouteRepository.deleteAll();
        searchRecordRepository.deleteAll();
    }
}
