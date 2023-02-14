package nl.bertriksikken.energymix.entsoe;

import java.time.Duration;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class EntsoeClientConfig {

    @JsonProperty("url")
    private final String url;

    @JsonProperty("timeout")
    private final int timeoutSec;

    @JsonProperty("apikey")
    private final String apiKey;

    // no-arg jackson constructor
    public EntsoeClientConfig() {
        this.url = "https://web-api.tp.entsoe.eu/api/";
        this.timeoutSec = 30;
        this.apiKey = "your-secret-key";
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

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "{url=%s,timeout=%d}", url, timeoutSec);
    }

}
