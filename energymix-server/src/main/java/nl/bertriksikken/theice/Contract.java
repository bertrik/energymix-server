package nl.bertriksikken.theice;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.bertriksikken.naturalgas.FutureGasPrices.FutureGasPrice;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Contract {

    // formatter/parser for the "lastTime" field
    public static final DateTimeFormatter LAST_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a z");

    @JsonProperty("volume")
    public final int volume;

    @JsonProperty("lastTime")
    private final String lastTime; // can be "null"

    @JsonProperty("endDate")
    public final String endDate;

    @JsonProperty("marketStrip")
    public final String marketStrip;

    @JsonProperty("change")
    public final double change;

    @JsonProperty("marketId")
    public final int marketId;

    @JsonProperty("lastPrice")
    public final Double lastPrice; // can be "null"

    // no-arg jackson constructor
    private Contract() {
        volume = 0;
        lastTime = "";
        endDate = "";
        marketStrip = "";
        change = Double.NaN;
        marketId = 0;
        lastPrice = Double.NaN;
    }

    public Instant getLastTime() {
        return lastTime != null ? LAST_TIME_FORMATTER.parse(lastTime, Instant::from) : null;
    }

    public FutureGasPrice toFutureGasPrice() {
        if ((lastPrice == null) || (lastTime == null) || (marketStrip == null)) {
            return null;
        }
        return new FutureGasPrice(lastPrice, getLastTime(), marketStrip);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%.3f @ %s for %s", lastPrice, getLastTime(), marketStrip);
    }
}
