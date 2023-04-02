package nl.bertriksikken.energymix.server;

import nl.bertriksikken.eex.EexClient;
import nl.bertriksikken.eex.EexConfig;
import nl.bertriksikken.theice.IceClient;
import nl.bertriksikken.theice.IceConfig;

/**
 * Runs the NGP download process.
 */
public final class RunNaturalGasHandler {

    public static void main(String args[]) {
        EexConfig eexConfig = new EexConfig();
        EexClient eexClient = EexClient.create(eexConfig);
        IceConfig iceConfig = new IceConfig();
        IceClient iceClient = IceClient.create(iceConfig);
        NaturalGasHandler handler = new NaturalGasHandler(eexClient, iceClient);
        handler.start();
    }

}
