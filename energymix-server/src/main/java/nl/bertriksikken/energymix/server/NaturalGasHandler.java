package nl.bertriksikken.energymix.server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.bertriksikken.naturalgas.NeutralGasPrices;
import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice;
import nl.bertriksikken.powernext.CurrentPriceDocument;
import nl.bertriksikken.powernext.FileResponse;
import nl.bertriksikken.powernext.PowernextClient;
import nl.bertriksikken.powernext.PowernextConfig;

/**
 * Downloads the TTF neutral gas price (NGP) on a schedule.
 */
public final class NaturalGasHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NaturalGasHandler.class);

    private static final Duration GAS_DOWNLOAD_INTERVAL = Duration.ofMinutes(15);

    private final PowernextClient powernextClient;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private NeutralGasPrices neutralGasPrice = new NeutralGasPrices(Instant.now());

    public NaturalGasHandler(PowernextConfig powernextConfig) {
        this.powernextClient = PowernextClient.create(powernextConfig);
    }

    public void start() {
        // start download immediately
        executor.execute(new CatchingRunnable(LOG, this::downloadGasPrices));
    }

    public void stop() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Caught InterruptedException", e);
        }
    }

    private void downloadGasPrices() {
        LOG.info("Download NGP current price document");

        // download and try to parse
        Instant next;
        try {
            FileResponse response = powernextClient.getCurrentPriceDocument();
            LOG.info("Downloaded NGP current price document, {} bytes, last modified {}",
                    response.getContents().length(), response.getLastModified());

            // get price for today
            NeutralGasPrices prices = CurrentPriceDocument.parse(response);
            NeutralGasDayPrice finalPrice = prices.findFinalPrice();
            if (finalPrice != null) {
                LOG.info("Final neutral gas price: {} EUR/MWh @ {}", finalPrice.indexValue, finalPrice.date);
                setGasPrices(prices);
            }
            next = response.getLastModified().plus(GAS_DOWNLOAD_INTERVAL).plusSeconds(30);
        } catch (IOException e) {
            LOG.warn("Download NGP current price document failed: {}", e.getMessage());
            next = Instant.now().plus(Duration.ofMinutes(5));
        }

        // schedule new download
        while (next.isBefore(Instant.now())) {
            next = next.plus(GAS_DOWNLOAD_INTERVAL);
        }
        Duration delay = Duration.between(Instant.now(), next).truncatedTo(ChronoUnit.SECONDS);
        LOG.info("Schedule next download in {} at {}", delay, next);
        executor.schedule(new CatchingRunnable(LOG, this::downloadGasPrices), delay.toSeconds(), TimeUnit.SECONDS);
    }

    // get a copy of the neutral gas price
    public synchronized NeutralGasPrices getGasPrices() {
        return new NeutralGasPrices(neutralGasPrice);
    }

    private synchronized void setGasPrices(NeutralGasPrices neutralGasPrice) {
        this.neutralGasPrice = neutralGasPrice;
    }

}
