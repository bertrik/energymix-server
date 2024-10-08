package nl.bertriksikken.energymix.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    
    private static final class PriceElement {
        @JsonProperty("time")
        private final long time;

        @JsonProperty("price")
        private final Double price;

        public PriceElement(Instant time, double price) {
            this.time = time.getEpochSecond();
            this.price = price;
        }
        
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%.2f@%d", price, time);
        }
    }
}
