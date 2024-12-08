package nl.bertriksikken.ned;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class GranularityTest {

    @Test
    public void testDuration() {
        Assertions.assertEquals(600, EGranularity.TEN_MINUTES.getDuration().toSeconds());
        Assertions.assertEquals(86400, EGranularity.DAY.getDuration().toSeconds());
    }

    @Test
    public void testFromDescriptor() {
        Assertions.assertEquals(EGranularity.FIFTEEN_MINUTES, EGranularity.fromDescriptor("/v1/granularities/4"));
    }

}
