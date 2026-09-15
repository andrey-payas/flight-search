package com.paias.air;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

public class TestBaseWiremock extends TestBase {
    public static WireMockServer wireMock = new WireMockServer(8189);
    @BeforeAll
    static void beforeAll() {
        wireMock.start();
        configureFor(wireMock.port());
    }
    @AfterAll
    static void afterAll() {
        wireMock.stop();
    }
}
