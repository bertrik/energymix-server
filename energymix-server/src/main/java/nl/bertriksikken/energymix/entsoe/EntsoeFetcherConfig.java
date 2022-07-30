package nl.bertriksikken.energymix.entsoe;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class EntsoeFetcherConfig {

    @JsonProperty("url")
    private final String url;

    @JsonProperty("timeout")
    private final int timeoutSec;

    @JsonProperty("apiKey")
    private final String apiKey;

    @JsonProperty("timeZone")
    private final String timeZone;

    // no-arg jackson constructor
    public EntsoeFetcherConfig() {
        this.url = "https://transparency.entsoe.eu";
        this.timeoutSec = 30;
        this.apiKey = "your-secret-key";
        this.timeZone = "Europe/Amsterdam";
    }

    public String getUrl() {
        return url;
    }

    public Duration getTimeout() {
        return Duration.ofSeconds(timeoutSec);
    }

    public String getApiKey() {
        return apiKey;
    }

    public ZoneId getTimeZone() {
        return ZoneId.of(timeZone);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "{url=%s,timeout=%d}", url, timeoutSec);
    }

}
