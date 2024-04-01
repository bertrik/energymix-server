package nl.bertriksikken.energymix.entsoe;

import java.time.Duration;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.bertriksikken.energymix.app.RestApiConfig;
import nl.bertriksikken.entsoe.EArea;

public final class EntsoeConfig extends RestApiConfig {

    @JsonProperty("apikey")
    private final String apiKey;

    @JsonProperty("area")
    private final String area = EArea.NETHERLANDS.getCode();

    @JsonProperty("timezone")
    private String timeZone = "Europe/Amsterdam";

    @JsonProperty("forecastOffset")
    private long forecastOffsetMinutes = 30;

    // no-arg jackson constructor
    public EntsoeConfig() {
        super("https://web-api.tp.entsoe.eu", 30);
        this.apiKey = "your-entsoe-api-key";
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getArea() {
        return area;
    }

    public ZoneId getTimeZone() {
        return ZoneId.of(timeZone);
    }

    public Duration getForecastOffset() {
        return Duration.ofMinutes(forecastOffsetMinutes);
    }

}
