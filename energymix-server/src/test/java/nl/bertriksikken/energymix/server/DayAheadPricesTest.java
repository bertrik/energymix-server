package nl.bertriksikken.energymix.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class DayAheadPricesTest {

    @Test
    public void testFormat() throws JsonProcessingException {
        Instant now = Instant.parse("2025-10-25T09:00:00Z");
        DayAheadPrices prices = new DayAheadPrices(now, 12.34);
        now = now.plus(15, ChronoUnit.MINUTES);
        prices.addPrice(now, 23.45);

        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        String json = writer.writeValueAsString(prices);

        System.out.println(json);
    }

}
