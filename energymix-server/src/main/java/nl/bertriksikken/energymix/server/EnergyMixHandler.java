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
import nl.bertriksikken.energymix.entsoe.EntsoeFetcher;
import nl.bertriksikken.entsoe.EArea;
import nl.bertriksikken.entsoe.EDocumentType;
import nl.bertriksikken.entsoe.EProcessType;
import nl.bertriksikken.entsoe.EPsrType;
import nl.bertriksikken.entsoe.EntsoeParser;
import nl.bertriksikken.entsoe.EntsoeRequest;
import nl.bertriksikken.entsoe.EntsoeResponse;

/**
 * Main process, downloads a new CSV from berthub.eu and serves an EnergyMix
 * JSON message.
 */
public final class EnergyMixHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EnergyMixHandler.class);
    private static final CsvMapper CSV_MAPPER = new CsvMapper();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final BerthubFetcher fetcher;
    private final EntsoeFetcher entsoeFetcher;
    private ProductionData latest = null;

    public EnergyMixHandler(BerthubFetcher fetcher, EntsoeFetcher entsoeFetcher) {
        this.fetcher = Preconditions.checkNotNull(fetcher);
        this.entsoeFetcher = Preconditions.checkNotNull(entsoeFetcher);
    }

    public void start() {
        LOG.info("Starting");
        executor.execute(this::download);
    }

    // runs on the executor
    private void download() {
        Instant now = Instant.now();

        // get data from berthub
        Instant next = now.plus(Duration.ofMinutes(15));
        try {
            LOG.info("Fetching new data from berthub.eu");
            DownloadResult result = fetcher.download(now);
            next = result.getTime().plus(Duration.ofMinutes(16));
            ProductionDataCsv production = ProductionDataCsv.parse(CSV_MAPPER, result.getData());
            latest = production.getLatest();
        } catch (IOException e) {
            LOG.warn("Fetching/decoding latest production data failed!", e);
        }

        // get solar forecast from entso-e
        try {
            LOG.info("Fetching solar/wind forecast from entso-e");
            Double solar = fetchSolarForecast(now);
            if (Double.isFinite(solar)) {
                latest = latest.withSolar(solar);
            }
        } catch (IOException e) {
            LOG.warn("Failed to fetch solar forecast!");
        }
        LOG.info("Latest: {}", latest);

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

    private Double fetchSolarForecast(Instant now) throws IOException {
        Instant periodStart = now.truncatedTo(ChronoUnit.DAYS);
        Instant periodEnd = periodStart.plus(Duration.ofDays(1));
        EntsoeRequest request = new EntsoeRequest(EDocumentType.WIND_SOLAR_FORECAST, EProcessType.DAY_AHEAD,
                EArea.NETHERLANDS);
        request.setPeriod(periodStart, periodEnd);
        EntsoeResponse document = entsoeFetcher.getDocument(request);
        EntsoeParser parser = new EntsoeParser(document);
        return parser.findPoint(now, EPsrType.SOLAR);
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
        mix.addComponent("nuclear", latest.nuclear, "#00FF00");
        mix.addComponent("other", latest.other, "#444444");
        mix.addComponent("waste", latest.waste, "#444444");
        return mix;
    }

}
