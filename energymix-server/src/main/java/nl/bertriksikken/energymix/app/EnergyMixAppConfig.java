package nl.bertriksikken.energymix.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.core.Configuration;
import nl.bertriksikken.eex.EexConfig;
import nl.bertriksikken.energymix.entsoe.EntsoeClientConfig;
import nl.bertriksikken.energymix.entsog.EntsogClientConfig;
import nl.bertriksikken.energymix.server.EnergyMixConfig;
import nl.bertriksikken.theice.IceConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EnergyMixAppConfig extends Configuration {

    @JsonProperty("entsoe")
    public final EntsoeClientConfig entsoeFetcherConfig = new EntsoeClientConfig();

    @JsonProperty("entsog")
    public final EntsogClientConfig entsogConfig = new EntsogClientConfig();

    @JsonProperty("eex")
    public final EexConfig eexConfig = new EexConfig();

    @JsonProperty("ice")
    public final IceConfig iceConfig = new IceConfig();

    @JsonProperty("energymix")
    public final EnergyMixConfig energyMixConfig = new EnergyMixConfig();

}
