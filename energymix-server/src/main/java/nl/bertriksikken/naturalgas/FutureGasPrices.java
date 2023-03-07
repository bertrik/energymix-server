package nl.bertriksikken.naturalgas;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Format-independent month-ahead natural gas price, TTF future.
 */
public final class FutureGasPrices {

    private Instant creationTime;
    private final List<FutureGasPrice> prices = new ArrayList<>();

    public FutureGasPrices(Instant creationTime) {
        this.creationTime = creationTime;
    }

    // copy constructor
    public FutureGasPrices(FutureGasPrices original) {
        this(original.creationTime);
        original.prices.forEach(this::add);
    }

    public void add(FutureGasPrice price) {
        prices.add(price);
    }

    public List<FutureGasPrice> getPrices() {
        return List.copyOf(prices);
    }

    public static final class FutureGasPrice {
        public final double price;
        public final Instant time;
        public final String period;

        public FutureGasPrice(double price, Instant time, String period) {
            this.price = price;
            this.time = time;
            this.period = period;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%.3f @ %s for %s", price, time, period);
        }
    }
}
