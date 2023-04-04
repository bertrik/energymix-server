package nl.bertriksikken.energymix.entsoe;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.bertriksikken.energymix.app.RestApiConfig;

public final class EntsoeClientConfig extends RestApiConfig {

    @JsonProperty("apikey")
    private final String apiKey;

    // no-arg jackson constructor
    public EntsoeClientConfig() {
        super("https://web-api.tp.entsoe.eu/api/", 30);
        this.apiKey = "your-secret-key";
    }

    public String getApiKey() {
        return apiKey;
    }

}
