package nl.bertriksikken.energymix.server;

import nl.bertriksikken.eex.EexClient;
import nl.bertriksikken.eex.EexConfig;
import nl.bertriksikken.energymix.entsog.EntsogClient;
import nl.bertriksikken.energymix.entsog.EntsogClientConfig;
import nl.bertriksikken.theice.IceClient;
import nl.bertriksikken.theice.IceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class NaturalGasHandlerTest {

    private NaturalGasHandler naturalGasHandler;

    @BeforeEach
    public void beforeEach() {
        EexClient eexClient = EexClient.create(new EexConfig());
        IceClient iceClient = IceClient.create(new IceConfig());
        EntsogClient entsogClient = EntsogClient.create(new EntsogClientConfig());
        naturalGasHandler = new NaturalGasHandler(eexClient, iceClient, entsogClient);
    }

    @Test
    public void testMonth() {
        Assertions.assertTrue(naturalGasHandler.isMonth("Feb25"));
        Assertions.assertFalse(naturalGasHandler.isMonth("Feb25-Mar25"));
    }
}
