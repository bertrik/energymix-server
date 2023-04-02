package nl.bertriksikken.eex;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public final class EexConfig {

    @JsonProperty("url")
    private final String url;

    @JsonProperty("timeout")
    private final int timeoutSec;

    // no-arg jackson constructor
    public EexConfig() {
        this.url = "https://gasandregistry.eex.com";
        this.timeoutSec = 30;
    }

    public String getUrl() {
        return url;
    }

    public Duration getTimeout() {
        return Duration.ofSeconds(timeoutSec);
    }
}
