package nl.bertriksikken.energymix.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class DayAheadPrices {

    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");

    @SuppressWarnings("UnusedVariable")
    @JsonProperty("current")
    private final PriceElement current;

    @JsonProperty("day-ahead")
    private final List<PriceElement> dayAheadPrices = new ArrayList<>();

    public DayAheadPrices(Instant now, double currentPrice) {
        this.current = PriceElement.build(now, currentPrice);
    }

    public void addPrice(Instant time, double price) {
        dayAheadPrices.add(PriceElement.build(time, price));
    }

    private record PriceElement(@JsonProperty("datetime") String dateTime, @JsonProperty("time") long time,
                                @JsonProperty("price") double price) {
        private static PriceElement build(Instant instant, double price) {
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZONE).truncatedTo(ChronoUnit.MINUTES);
            String dateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime);
            return new PriceElement(dateTime, instant.getEpochSecond(), price);
        }
    }
}
