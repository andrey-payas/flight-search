package com.paias.air;

import com.paias.air.client.TequilaClient;
import com.paias.air.model.client.SearchResponse;
import com.paias.air.model.n4j.Flight;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.paias.air.WiremockUtils.stubSearch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TequilaClientTests extends TestBaseWiremock {
    @Autowired
    private TequilaClient tequilaClient;

    @Test
    void testParseSearchResponse() {
        stubSearch("responses/search.json");
        List<Flight> flights = tequilaClient.search("FRA", "01/12/2028", "03/04/2028");
        assertNotNull(flights);
        assertEquals(10, flights.size());
        assertNotNull(flights.getFirst().getToTime());
        assertNotNull(flights.getFirst().getLegs().getFirst().getFlyFrom());
    }
}
