package nl.bertriksikken.energymix.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class EnergyMixConfigTest {

    @Test
    public void testDefaults() {
        EnergyMixConfig config = new EnergyMixConfig();

        Assertions.assertNotNull(config.getArea());
        Assertions.assertNotNull(config.getTimeZone());
        Assertions.assertNotNull(config.getForecastOffset());
        Assertions.assertNotNull(config.toString());
    }

}
