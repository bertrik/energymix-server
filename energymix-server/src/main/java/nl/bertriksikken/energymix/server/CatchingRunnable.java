package nl.bertriksikken.energymix.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public final class CatchingRunnable implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(CatchingRunnable.class);

    private final Runnable runnable;

    public CatchingRunnable(Runnable runnable) {
        this.runnable = Preconditions.checkNotNull(runnable);
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Throwable e) {
            LOG.error("Caught throwable from runnable", e);
        }
    }

}
