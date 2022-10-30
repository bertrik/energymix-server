package nl.bertriksikken.energymix.server;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;

public final class CatchingRunnable implements Runnable {

    private final Logger logger;
    private final CheckedRunnable runnable;

    public CatchingRunnable(Logger logger, CheckedRunnable runnable) {
        this.logger = Preconditions.checkNotNull(logger);
        this.runnable = Preconditions.checkNotNull(runnable);
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Throwable e) {
            logger.warn("Caught throwable from runnable: {}", e.getMessage());
        }
    }

    public interface CheckedRunnable {
        public void run() throws Throwable;
    }

}
