package nl.bertriksikken.naturalgas;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice.ENgpStatus;

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
        original.dayPrices.forEach(this::add);
    }

    public void add(NeutralGasDayPrice entry) {
        dayPrices.add(entry);
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public NeutralGasDayPrice findFinalPrice() {
        return dayPrices.stream().filter(p -> p.status == ENgpStatus.FINAL).findFirst().orElse(null);
    }

    public List<NeutralGasDayPrice> getTemporaryPrices() {
        return dayPrices.stream().filter(p -> p.status == ENgpStatus.TEMPORARY).filter(p -> p.indexVolume > 0)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s @ %s", dayPrices, creationTime);
    }

    // one entry in the neutral gas price, for a specific day
    public static final class NeutralGasDayPrice {
        public final LocalDate date; // day in CET time zone
        public final double indexValue; // EUR/MWh
        public final int indexVolume; // MWh
        public final ENgpStatus status;
        public final Instant timestamp;

        public NeutralGasDayPrice(LocalDate date, double indexValue, int indexVolume, ENgpStatus status,
                Instant timestamp) {
            this.date = Objects.requireNonNull(date);
            this.indexValue = indexValue;
            this.indexVolume = indexVolume;
            this.status = status;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%.3f @ %s", indexValue, DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        }

        public static enum ENgpStatus {
            FINAL, TEMPORARY, UNKNOWN;
        }
    }

}
