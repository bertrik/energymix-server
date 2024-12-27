package nl.bertriksikken.naturalgas;

import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice.ENgpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * List of TTF neutral gas prices.
 * <p>
 * This is an internal representation, not tied to any specific format.
 */
public final class NeutralGasPrices {

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
                .toList();
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s @ %s", dayPrices, creationTime);
    }

    // one entry in the neutral gas price, for a specific day
    public record NeutralGasDayPrice(
            LocalDate date, // day in CET time zone
            double indexValue, // EUR/MWh
            int indexVolume, // MWh
            ENgpStatus status,
            Instant timestamp) {

        public enum ENgpStatus {
            FINAL, TEMPORARY, UNKNOWN
        }
    }

}
