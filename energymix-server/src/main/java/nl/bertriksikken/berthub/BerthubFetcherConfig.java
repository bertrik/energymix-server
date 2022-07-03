package nl.bertriksikken.berthub;

import java.time.Duration;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class BerthubFetcherConfig {

    @JsonProperty("url")
    private final String url;
    
    @JsonProperty("timeout")
    private final int timeoutSec;
    
    // no-arg jackson constructor
    public BerthubFetcherConfig() {
        this.url = "https://berthub.eu";
        this.timeoutSec = 30;
    }
    
    public String getUrl() {
        return url;
    }

    public Duration getTimeout() {
        return Duration.ofSeconds(timeoutSec);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "{url=%s,timeout=%d}", url, timeoutSec);
    }

}
