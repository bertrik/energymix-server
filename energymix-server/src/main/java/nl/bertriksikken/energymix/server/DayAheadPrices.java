package nl.bertriksikken.energymix.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class DayAheadPrices {

    @SuppressWarnings("UnusedVariable")
    @JsonProperty("current")
    private final PriceElement current;

    @JsonProperty("day-ahead")
    private final List<PriceElement> dayAheadPrices = new ArrayList<>();

    public DayAheadPrices(Instant now, double currentPrice) {
        this.current = new PriceElement(now, currentPrice);
    }

    public void addPrice(Instant time, double price) {
        dayAheadPrices.add(new PriceElement(time, price));
    }

    private record PriceElement(@JsonProperty("time") long time, @JsonProperty("price") double price) {
        public PriceElement(Instant time, double price) {
            this(time.getEpochSecond(), price);
        }
    }
}
