package nl.bertriksikken.energymix.app;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import nl.bertriksikken.berthub.BerthubFetcherConfig;

public final class EnergyMixAppConfig extends Configuration {

    @JsonProperty("berthub")
    public final BerthubFetcherConfig berthubConfig = new BerthubFetcherConfig();

}
