package com.paias.air;

import com.paias.air.util.FileUtils;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class WiremockUtils {
    public static void stubSearch(String fileName) {
        String body = FileUtils.readString(fileName);
        stubFor(get(urlPathMatching("/v2/search.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(body.getBytes())
                ));
    }

    public static void stubCountries() throws IOException {
        String emptyBody = FileUtils.readString(
                "responses/one_per_city/search_empty.json"
        );

        stubFor(get(urlPathEqualTo("/v2/search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyBody)
                ));

        stubSearchForCountry("FR", "search_FR.json");
        stubSearchForCountry("DE", "search_DE.json");
        stubSearchForCountry("ES", "search_ES.json");
        stubSearchForCountry("GB", "search_GB.json");
        stubSearchForCountry("IT", "search_IT.json");
        stubSearchForCountry("PL", "search_PL.json");

        stubSearchForDirectFlight("ES", "DE");
        stubSearchForDirectFlight("GB", "DE");
        stubSearchForDirectFlight("GB", "ES");
        stubSearchForDirectFlight("GB", "IT");
        stubSearchForDirectFlight("IT", "DE");
        stubSearchForDirectFlight("IT", "ES");
    }

    private static void stubSearchForCountry(String countryCode, String fileName) {
        String body = FileUtils.readString(
                "responses/one_per_city/" + fileName
        );

        stubFor(get(urlPathEqualTo("/v2/search"))
                .withQueryParam("fly_from", equalTo(countryCode))
                .withQueryParam("date_from", equalTo("01/11/2028"))
                .withQueryParam("date_to", equalTo("01/12/2028"))
                .withQueryParam("one_for_city", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                ));
    }

    private static void stubSearchForDirectFlight(String from, String to) {
        String body = FileUtils.readString(
                String.format("responses/direct/%s_%s.json", from, to));

        stubFor(get(urlPathEqualTo("/v2/search"))
                .withQueryParam("fly_from", equalTo(from))
                .withQueryParam("fly_to", equalTo(to))
                .withQueryParam("date_from", equalTo("01/11/2028"))
                .withQueryParam("date_to", equalTo("01/12/2028"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                ));
    }
}
