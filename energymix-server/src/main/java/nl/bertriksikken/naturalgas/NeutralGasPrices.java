package nl.bertriksikken.naturalgas;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * List of TTF neutral gas prices.
 * <p>
 * This is an internal representation, not tied to any specific format.
 */
public final class NeutralGasPrices {

    public static final ZoneId NGP_TIME_ZONE = ZoneId.of("CET");

    private final Instant creationTime;
    private final List<NeutralGasDayPrice> dayPrices = new ArrayList<>();

    public NeutralGasPrices(Instant creationTime) {
        this.creationTime = creationTime;
    }

    // copy-constructor
    public NeutralGasPrices(NeutralGasPrices original) {
        this(original.creationTime);
        original.getDayPrices().forEach(entry -> add(entry));
    }

    public void add(NeutralGasDayPrice entry) {
        dayPrices.add(entry);
    }

    public NeutralGasDayPrice findDayPrice(LocalDate date) {
        return dayPrices.stream().filter(entry -> entry.date.equals(date)).findFirst().orElse(null);
    }

    // one entry in the neutral gas price, for a specific day
    public static final class NeutralGasDayPrice {
        public final LocalDate date; // day in CET time zone
        public final double indexValue; // EUR/MWh
        public final int indexVolume; // MWh
        public final ENgpStatus status;

        public NeutralGasDayPrice(LocalDate date, double indexValue, int indexVolume, ENgpStatus status) {
            this.date = Preconditions.checkNotNull(date);
            this.indexValue = indexValue;
            this.indexVolume = indexVolume;
            this.status = status;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "{%s: %.3f, %s}", date, indexValue, status);
        }

        public static enum ENgpStatus {
            FINAL, TEMPORARY, UNKNOWN;
        }
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public List<NeutralGasDayPrice> getDayPrices() {
        return ImmutableList.copyOf(dayPrices);
    }

}
