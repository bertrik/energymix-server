package nl.bertriksikken.energymix.server;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.bertriksikken.entsoe.EArea;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public final class EnergyMixConfig {

    @JsonProperty("area")
    private final String area = EArea.NETHERLANDS.getCode();

    @JsonProperty("timezone")
    private String timeZone = "Europe/Amsterdam";

    @JsonProperty("forecastOffset")
    private long forecastOffsetMinutes = 30;

    public String getArea() {
        return area;
    }

    public ZoneId getTimeZone() {
        return ZoneId.of(timeZone);
    }

    public Duration getForecastOffset() {
        return Duration.ofMinutes(forecastOffsetMinutes);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "{%s,%s,%d}", timeZone, area, forecastOffsetMinutes);
    }
}
