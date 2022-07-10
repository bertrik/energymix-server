package nl.bertriksikken.energymix.server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.common.base.Preconditions;

import nl.bertriksikken.berthub.BerthubFetcher;
import nl.bertriksikken.berthub.BerthubFetcher.DownloadResult;
import nl.bertriksikken.berthub.ProductionData;
import nl.bertriksikken.berthub.ProductionDataCsv;

/**
 * Main process, downloads a new CSV from berthub.eu and serves an EnergyMix JSON message.
 */
public final class EnergyMixHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EnergyMixHandler.class);
    private static final CsvMapper CSV_MAPPER = new CsvMapper();
    
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    
    private final BerthubFetcher fetcher;
    private ProductionData latest = null;
    
    public EnergyMixHandler(BerthubFetcher fetcher) {
        this.fetcher = Preconditions.checkNotNull(fetcher);
    }

    public void start() {
        LOG.info("Starting");
        executor.execute(this::download);
    }
    
    // runs on the executor
    private void download() {
        Instant now = Instant.now();
        Instant next = now.plus(Duration.ofMinutes(15));
        try {
            LOG.info("Getting new data from berthub.eu");
            DownloadResult result = fetcher.download(now);
            next = result.getTime().plus(Duration.ofMinutes(16));
            ProductionDataCsv production = ProductionDataCsv.parse(CSV_MAPPER, result.getData());
            latest = production.getLatest();
            LOG.info("Latest: {}", latest);
        } catch (IOException e) {
            LOG.warn("Fetching/decoding latest production data failed!", e);
        }

        // schedule next
        Duration delay = Duration.between(now, next).truncatedTo(ChronoUnit.SECONDS);
        if (delay.isNegative()) {
            // time calculation is off, retry in 5 minutes
            LOG.warn("Time schedule calculation error");
            delay = Duration.ofMinutes(5);
        }
        executor.schedule(this::download, delay.toSeconds(), TimeUnit.SECONDS);
        LOG.info("Scheduled next download for {} (in {})", next, delay);
    }

    public void stop() throws InterruptedException {
        LOG.info("Stopping");
        executor.shutdownNow();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * @return the latest results
     */
    public EnergyMix getLatest() {
        try {
            return executor.submit(this::convertLatest).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Caught exception handling request", e);
            return null;
        }
    }

    // runs on the executor
    private EnergyMix convertLatest() {
        if (latest == null) {
            return new EnergyMix(0, 0);
        }
        
        long total = (long) latest.getTotal();
        EnergyMix mix = new EnergyMix(latest.time.getEpochSecond(), total);
        mix.addComponent("solar", latest.solar, "#FFFF00");
        mix.addComponent("wind", latest.wind, "#0000FF");
        mix.addComponent("fossil", latest.fossil, "#FF0000");
        mix.addComponent("nuclear", latest.nuclear, "#FF00FF");
        mix.addComponent("other", latest.other, "#444444");
        mix.addComponent("waste", latest.waste, "#444444");
        return mix;
    }

}
