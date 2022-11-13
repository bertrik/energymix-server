package nl.bertriksikken.energymix.server;

import nl.bertriksikken.powernext.PowernextClient;
import nl.bertriksikken.powernext.PowernextConfig;
import nl.bertriksikken.theice.IceClient;
import nl.bertriksikken.theice.IceConfig;

/**
 * Runs the NGP download process.
 */
public final class RunNaturalGasHandler {

    public static void main(String args[]) {
        PowernextConfig powernextConfig = new PowernextConfig();
        PowernextClient powernextClient = PowernextClient.create(powernextConfig);
        IceConfig iceConfig = new IceConfig();
        IceClient iceClient = IceClient.create(iceConfig);
        NaturalGasHandler handler = new NaturalGasHandler(powernextClient, iceClient);
        handler.start();
    }

}
