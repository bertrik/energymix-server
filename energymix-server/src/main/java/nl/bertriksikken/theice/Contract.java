package nl.bertriksikken.theice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Contract {

    @JsonProperty("volume")
    public final int volume;

    @JsonProperty("lastTime")
    public final String lastTime; // can be "null"

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
}
