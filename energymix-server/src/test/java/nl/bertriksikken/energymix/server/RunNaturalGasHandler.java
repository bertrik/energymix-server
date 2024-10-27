package nl.bertriksikken.energymix.server;

import nl.bertriksikken.eex.EexClient;
import nl.bertriksikken.eex.EexConfig;
import nl.bertriksikken.energymix.entsog.EntsogClient;
import nl.bertriksikken.energymix.entsog.EntsogClientConfig;
import nl.bertriksikken.theice.IceClient;
import nl.bertriksikken.theice.IceConfig;

/**
 * Runs the NGP download process.
 */
public final class RunNaturalGasHandler {

    public static void main(String args[]) {
        EexConfig eexConfig = new EexConfig();
        try (EexClient eexClient = EexClient.create(eexConfig)) {
            IceConfig iceConfig = new IceConfig();
            IceClient iceClient = IceClient.create(iceConfig);
            EntsogClientConfig entsogConfig = new EntsogClientConfig();
            EntsogClient entsogClient = EntsogClient.create(entsogConfig);
            NaturalGasHandler handler = new NaturalGasHandler(eexClient, iceClient, entsogClient);
            handler.start();
        }
    }

}
