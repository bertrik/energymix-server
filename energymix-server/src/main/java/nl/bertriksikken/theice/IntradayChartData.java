package nl.bertriksikken.theice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class IntradayChartData {

    @JsonProperty("lastPriceChangeDirection")
    public final String lastPriceChangeDirection;

    @JsonProperty("change")
    public final double change;

    @JsonProperty("exchangeImage")
    public final String exchangeImage;

    @JsonProperty("stripType")
    public final EStripType stripType;

    @JsonProperty("contractName")
    public final String contractName;

    @JsonProperty("exchange")
    public final String exchange;

    @JsonProperty("settlementPrice")
    public final double settlementPrice;

    @JsonProperty("bars")
    public final List<Bar> bars;

    @JsonProperty("percentChangeDirection")
    public final EChangeDirection percentChangeDirection;

    @JsonProperty("marketId")
    public final int marketId;

    @JsonProperty("stripDescription")
    public final String stripDescription;

    @JsonProperty("lastPrice")
    public final double lastPrice;

    // jackson constructor
    private IntradayChartData() {
        this.lastPriceChangeDirection = "";
        this.change = Double.NaN;
        this.exchangeImage = "";
        this.stripType = EStripType.UNKNOWN;
        this.contractName = "";
        this.exchange = "";
        this.settlementPrice = Double.NaN;
        this.bars = new ArrayList<>();
        this.percentChangeDirection = EChangeDirection.UNKNOWN;
        this.marketId = 0;
        this.stripDescription = "";
        this.lastPrice = Double.NaN;
    }

    public enum EStripType {
        MONTH("month"), // month
        UNKNOWN("unknown");

        private final String description;

        EStripType(String description) {
            this.description = description;
        }

        @JsonCreator
        static EStripType from(String description) {
            return Stream.of(values()).filter(v -> v.description.equals(description)).findFirst().orElse(UNKNOWN);
        }
    }

    public enum EChangeDirection {
        UP("up"), // up
        DOWN("down"), // down
        UNKNOWN("unknown");

        private final String description;

        EChangeDirection(String description) {
            this.description = description;
        }

        @JsonCreator
        static EChangeDirection from(String description) {
            return Stream.of(values()).filter(v -> v.description.equals(description)).findFirst().orElse(UNKNOWN);
        }
    }

    /**
     * "Wed Nov 09 07:02:00 2022", 115
     */
    @SuppressWarnings("serial")
    public static class Bar extends ArrayList<String> {
        public String getDate() {
            return get(0);
        }

        public double getPrice() {
            return Double.parseDouble(get(1));
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%.3f @ %s", getPrice(), getDate());
        }
    }
}
