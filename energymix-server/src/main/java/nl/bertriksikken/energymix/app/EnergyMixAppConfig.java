package nl.bertriksikken.energymix.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import nl.bertriksikken.energymix.entsoe.EntsoeClientConfig;
import nl.bertriksikken.energymix.server.EnergyMixConfig;
import nl.bertriksikken.powernext.PowernextConfig;
import nl.bertriksikken.theice.IceConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EnergyMixAppConfig extends Configuration {

    @JsonProperty("entsoe")
    public final EntsoeClientConfig entsoeFetcherConfig = new EntsoeClientConfig();

    @JsonProperty("powernext")
    public final PowernextConfig powerNextConfig = new PowernextConfig();

    @JsonProperty("ice")
    public final IceConfig iceConfig = new IceConfig();

    @JsonProperty("energymix")
    public final EnergyMixConfig energyMixConfig = new EnergyMixConfig();

}
