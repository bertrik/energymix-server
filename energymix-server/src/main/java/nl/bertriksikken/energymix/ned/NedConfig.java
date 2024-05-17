package nl.bertriksikken.energymix.ned;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.bertriksikken.energymix.app.RestApiConfig;

public final class NedConfig extends RestApiConfig {

    @JsonProperty("apikey")
    private final String apiKey;

    public NedConfig() {
        super("https://api.ned.nl", 30);
        this.apiKey = "your-api-key";
    }

    public String getApiKey() {
        return apiKey;
    }

}
