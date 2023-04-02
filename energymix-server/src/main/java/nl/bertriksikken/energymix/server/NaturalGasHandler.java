package nl.bertriksikken.energymix.server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import nl.bertriksikken.eex.CurrentPriceDocument;
import nl.bertriksikken.eex.FileResponse;
import nl.bertriksikken.eex.EexClient;
import nl.bertriksikken.naturalgas.FutureGasPrices;
import nl.bertriksikken.naturalgas.FutureGasPrices.FutureGasPrice;
import nl.bertriksikken.naturalgas.NeutralGasPrices;
import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice;
import nl.bertriksikken.theice.Contract;
import nl.bertriksikken.theice.IceClient;

/**
 * Downloads the TTF neutral gas price (NGP) on a schedule.
 */
public final class NaturalGasHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NaturalGasHandler.class);

    private static final Duration GAS_DOWNLOAD_INTERVAL = Duration.ofMinutes(15);
    private static final Duration ICE_DOWNLOAD_INTERVAL = Duration.ofMinutes(15);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final EexClient eexClient;
    private final IceClient iceClient;

    private NeutralGasPrices neutralGasPrice = new NeutralGasPrices(Instant.now());
    private FutureGasPrices futureGasPrices = new FutureGasPrices(Instant.now());

    public NaturalGasHandler(EexClient eexClient, IceClient iceClient) {
        this.eexClient = Objects.requireNonNull(eexClient);
        this.iceClient = Objects.requireNonNull(iceClient);
    }

    public void start() {
        // start download immediately
        executor.execute(new CatchingRunnable(LOG, this::downloadIceContracts));
        executor.execute(new CatchingRunnable(LOG, this::downloadEexNGP));
    }

    public void stop() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Caught InterruptedException", e);
        }
    }

    private void downloadEexNGP() {
        LOG.info("Download EEX NGP current price document");

        // download and try to parse
        Instant next = Instant.now().plus(Duration.ofMinutes(5));
        try {
            FileResponse response = eexClient.getCurrentPriceDocument();
            LOG.info("Downloaded NGP current price document, {} bytes, last modified {}",
                    response.getContents().length(), response.getLastModified());

            // get price for today
            NeutralGasPrices prices = CurrentPriceDocument.parse(response);
            LOG.info("Neutral gas prices: {}", prices);
            NeutralGasDayPrice finalPrice = prices.findFinalPrice();
            if (finalPrice != null) {
                setNeutralGasPrices(prices);
            }
            next = response.getLastModified().plus(GAS_DOWNLOAD_INTERVAL).plusSeconds(30);
        } catch (IOException e) {
            LOG.warn("Download NGP current price document failed: {}", e.getMessage());
        }

        // schedule new download
        while (next.isBefore(Instant.now())) {
            next = next.plus(GAS_DOWNLOAD_INTERVAL);
        }
        Duration delay = Duration.between(Instant.now(), next).truncatedTo(ChronoUnit.SECONDS);
        LOG.info("Schedule next EEX download in {} at {}", delay, next);
        executor.schedule(new CatchingRunnable(LOG, this::downloadEexNGP), delay.toSeconds(), TimeUnit.SECONDS);
    }

    private void downloadIceContracts() {
        LOG.info("Download ICE gas contracts");

        Instant next = Instant.now().plus(ICE_DOWNLOAD_INTERVAL);
        try {
            FutureGasPrices futureGasPrices = new FutureGasPrices(Instant.now());
            List<Contract> contracts = iceClient.getContracts();
            contracts.stream().map(Contract::toFutureGasPrice).filter(Objects::nonNull).filter(p -> isMonth(p.period))
                    .forEach(futureGasPrices::add);
            FutureGasPrice monthAheadGasPrice = Iterables.getFirst(futureGasPrices.getPrices(), null);
            if (monthAheadGasPrice != null) {
                LOG.info("ICE month ahead price: {}", monthAheadGasPrice);
                setFutureGasPrices(futureGasPrices);
                next = Instant.now().truncatedTo(ChronoUnit.HOURS);
            }
        } catch (IOException e) {
            LOG.warn("Download ICE gas contracts failed: {}", e.getMessage());
        }

        // schedule new download
        while (next.isBefore(Instant.now())) {
            next = next.plus(ICE_DOWNLOAD_INTERVAL);
        }
        Duration delay = Duration.between(Instant.now(), next);
        LOG.info("Schedule next ICE download in {} at {}", delay, next);
        executor.schedule(new CatchingRunnable(LOG, this::downloadIceContracts), delay.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    private boolean isMonth(String month) {
        return Stream.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                .anyMatch(month::startsWith);
    }

    // get a copy of the neutral gas price
    public synchronized NeutralGasPrices getNeutralGasPrices() {
        return new NeutralGasPrices(neutralGasPrice);
    }

    private synchronized void setNeutralGasPrices(NeutralGasPrices neutralGasPrice) {
        this.neutralGasPrice = neutralGasPrice;
    }

    // get a copy of the month-ahead gas price
    public synchronized FutureGasPrices getFutureGasPrices() {
        return new FutureGasPrices(futureGasPrices);
    }

    private synchronized void setFutureGasPrices(FutureGasPrices futureGasPrices) {
        this.futureGasPrices = futureGasPrices;
    }

}
