package nl.bertriksikken.energymix.entsog;

import nl.bertriksikken.energymix.app.RestApiConfig;

public final class EntsogClientConfig extends RestApiConfig {

    // no-arg jackson constructor
    public EntsogClientConfig() {
        super("https://transparency.entsog.eu", 60);
    }

}
