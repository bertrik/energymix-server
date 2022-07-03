package nl.bertriksikken.energymix.server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;

import nl.bertriksikken.berthub.BerthubFetcher;
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
        executor.scheduleAtFixedRate(this::download, 0, 15, TimeUnit.MINUTES);
    }
    
    // runs on the executor
    private void download() {
        Instant now = Instant.now();
        try {
            LOG.info("Getting new data from berthub.eu");
            String csvData = fetcher.download(now);
            ProductionDataCsv production = ProductionDataCsv.parse(CSV_MAPPER, csvData);
            latest = production.getLatest();
            LOG.info("Latest: {}", latest);
        } catch (IOException e) {
            LOG.warn("Fetching/decoding latest production data failed!", e);
        }
    }

    public void stop() {
        LOG.info("Stopping");
        MoreExecutors.shutdownAndAwaitTermination(executor, Duration.ofSeconds(10));
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
        mix.addComponent("biomass", percent(latest.biomass, total), "#00FF00");
        mix.addComponent("fossil", percent(latest.fossil, total), "#FF0000");
        mix.addComponent("nuclear", percent(latest.nuclear, total), "#FF00FF");
        mix.addComponent("other", percent(latest.other, total), "#444444");
        mix.addComponent("solar", percent(latest.solar, total), "#FFFF00");
        mix.addComponent("waste", percent(latest.waste, total), "#444444");
        mix.addComponent("wind", percent(latest.wind, total), "#0000FF");
        return mix;
    }
    
    private int percent(double value, double total) {
        return (int)Math.round(100.0 * value / total);
    }

}
