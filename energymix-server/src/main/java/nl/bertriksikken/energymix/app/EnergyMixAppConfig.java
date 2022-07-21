package nl.bertriksikken.energymix.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import nl.bertriksikken.energymix.entsoe.EntsoeFetcherConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EnergyMixAppConfig extends Configuration {

    @JsonProperty("entsoe")
    public final EntsoeFetcherConfig entsoeConfig = new EntsoeFetcherConfig();
}
