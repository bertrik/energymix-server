package nl.bertriksikken.energymix.server;

import java.util.Objects;

import org.slf4j.Logger;

public final class CatchingRunnable implements Runnable {

    private final Logger logger;
    private final CheckedRunnable runnable;

    public CatchingRunnable(Logger logger, CheckedRunnable runnable) {
        this.logger = Objects.requireNonNull(logger);
        this.runnable = Objects.requireNonNull(runnable);
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Throwable e) {
            logger.warn("Caught throwable from runnable", e);
        }
    }

    public interface CheckedRunnable {
        void run() throws Throwable;
    }

}
