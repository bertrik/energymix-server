package nl.bertriksikken.ned;

import org.junit.Assert;
import org.junit.Test;

public final class GranularityTest {

    @Test
    public void testDuration() {
        Assert.assertEquals(600, EGranularity.TEN_MINUTES.getDuration().toSeconds());
        Assert.assertEquals(86400, EGranularity.DAY.getDuration().toSeconds());
    }

    @Test
    public void testFromDescriptor() {
        Assert.assertEquals(EGranularity.FIFTEEN_MINUTES, EGranularity.fromDescriptor("/v1/granularities/4"));
    }

}
