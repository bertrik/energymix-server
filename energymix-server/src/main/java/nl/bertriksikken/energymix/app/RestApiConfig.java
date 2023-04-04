package nl.bertriksikken.energymix.app;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class RestApiConfig {

    @JsonProperty("url")
    private final String url;

    @JsonProperty("timeout")
    private final int timeoutSec;

    // no-arg jackson constructor
    @SuppressWarnings("unused")
    private RestApiConfig() {
        this("", 0);
    }

    public RestApiConfig(String url, int timeoutSec) {
        this.url = Objects.requireNonNull(url);
        this.timeoutSec = timeoutSec;
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
