package nl.bertriksikken.energymix.ned;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.bertriksikken.energymix.app.RestApiConfig;

import java.time.Duration;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class NedConfig extends RestApiConfig {

    @JsonProperty("apikey")
    private final String apiKey;

    @JsonProperty("interval")
    private final int interval;

    public NedConfig() {
        super("https://api.ned.nl", 30);
        this.apiKey = "your-api-key";
        this.interval = 600;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Duration getInterval() {
        return Duration.ofSeconds(interval);
    }

}
