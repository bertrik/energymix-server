package nl.bertriksikken.energymix.server;

import org.junit.Assert;
import org.junit.Test;

public final class EnergyMixConfigTest {

    @Test
    public void testDefaults() {
        EnergyMixConfig config = new EnergyMixConfig();

        Assert.assertNotNull(config.getArea());
        Assert.assertNotNull(config.getTimeZone());
        Assert.assertNotNull(config.getForecastOffset());
        Assert.assertNotNull(config.toString());
    }

}
