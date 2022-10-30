package nl.bertriksikken.energymix.server;

import nl.bertriksikken.powernext.PowernextConfig;

/**
 * Runs the NGP download process.
 */
public final class RunNaturalGasHandler {

    public static void main(String args[]) {
        PowernextConfig config = new PowernextConfig();
        NaturalGasHandler handler = new NaturalGasHandler(config);
        handler.start();
    }

}
