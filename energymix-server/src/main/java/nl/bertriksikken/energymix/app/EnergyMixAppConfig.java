package nl.bertriksikken.energymix.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import nl.bertriksikken.berthub.BerthubFetcherConfig;
import nl.bertriksikken.energymix.entsoe.EntsoeFetcherConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EnergyMixAppConfig extends Configuration {

    @JsonProperty("berthub")
    public final BerthubFetcherConfig berthubConfig = new BerthubFetcherConfig();

    @JsonProperty("entsoe")
    public final EntsoeFetcherConfig entsoeConfig = new EntsoeFetcherConfig();
}
