package nl.bertriksikken.energymix.server;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CatchingRunnableTest {

    private static final Logger LOG = LoggerFactory.getLogger(CatchingRunnableTest.class);

    @Test
    public void testNoEscape() {
        CatchingRunnable runnable = new CatchingRunnable(LOG, () -> {throw new RuntimeException("fake");});
        runnable.run();
        // expect no exception
    }

}
