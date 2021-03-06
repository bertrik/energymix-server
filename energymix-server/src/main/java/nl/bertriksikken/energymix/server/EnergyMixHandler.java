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

import com.google.common.base.Preconditions;

import nl.bertriksikken.energymix.entsoe.EntsoeFetcher;
import nl.bertriksikken.entsoe.EArea;
import nl.bertriksikken.entsoe.EDocumentType;
import nl.bertriksikken.entsoe.EProcessType;
import nl.bertriksikken.entsoe.EPsrType;
import nl.bertriksikken.entsoe.EntsoeParser;
import nl.bertriksikken.entsoe.EntsoeParser.Result;
import nl.bertriksikken.entsoe.EntsoeRequest;
import nl.bertriksikken.entsoe.EntsoeResponse;

/**
 * Main process, downloads a new CSV from berthub.eu and serves an EnergyMix
 * JSON message.
 */
public final class EnergyMixHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EnergyMixHandler.class);
    private static final Duration ENTSO_INTERVAL = Duration.ofMinutes(15);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final EntsoeFetcher entsoeFetcher;
    private EnergyMix energyMix;

    public EnergyMixHandler(EntsoeFetcher entsoeFetcher) {
        this.entsoeFetcher = Preconditions.checkNotNull(entsoeFetcher);
    }

    public void start() {
        LOG.info("Starting");
        // schedule immediately
        executor.execute(new CatchingRunnable(this::downloadFromEntsoe));
    }

    // runs on the executor
    private void downloadFromEntsoe() {
        Instant now = Instant.now();
        Instant periodStart = now.truncatedTo(ChronoUnit.DAYS);
        Instant periodEnd = periodStart.plus(Duration.ofDays(1));
        try {
            // get actual generation by type
            LOG.info("Downloading actual generation per type");
            EntsoeRequest actualGenerationRequest = new EntsoeRequest(EDocumentType.ACTUAL_GENERATION_PER_TYPE,
                    EProcessType.REALISED, EArea.NETHERLANDS);
            actualGenerationRequest.setPeriod(periodStart, periodEnd);
            EntsoeResponse actualGenerationResponse = entsoeFetcher.getDocument(actualGenerationRequest);
            EntsoeParser actualGenerationParser = new EntsoeParser(actualGenerationResponse);
            Result fossil = sumGeneration(actualGenerationParser, EPsrType.FOSSIL_HARD_COAL, EPsrType.FOSSIL_GAS);
            Result nuclear = sumGeneration(actualGenerationParser, EPsrType.NUCLEAR);
            Result wind = sumGeneration(actualGenerationParser, EPsrType.WIND_ONSHORE, EPsrType.WIND_OFFSHORE);
            Result other = sumGeneration(actualGenerationParser, EPsrType.RENEWABLE_OTHER, EPsrType.OTHER);
            Result waste = sumGeneration(actualGenerationParser, EPsrType.WASTE);
            LOG.info("Fossil result: {}, age {}", fossil, Duration.between(fossil.time, now));

            // get solar/wind prediction
            LOG.info("Downloading wind/solar forecast");
            EntsoeRequest windSolarForecastRequest = new EntsoeRequest(EDocumentType.WIND_SOLAR_FORECAST,
                    EProcessType.DAY_AHEAD, EArea.NETHERLANDS);
            windSolarForecastRequest.setPeriod(periodStart, periodEnd);
            EntsoeResponse solarForecastResponse = entsoeFetcher.getDocument(windSolarForecastRequest);
            EntsoeParser solarWindParser = new EntsoeParser(solarForecastResponse);
            Result solar = solarWindParser.findByTime(fossil.time, EPsrType.SOLAR);
            LOG.info("Solar result: {}", solar);

            // build energy mix structure
            energyMix = new EnergyMix(fossil.time.getEpochSecond());
            energyMix.addComponent("solar", solar.value, "#FFFF00");
            energyMix.addComponent("wind", wind.value, "#0000FF");
            energyMix.addComponent("fossil", fossil.value, "#FF0000");
            energyMix.addComponent("nuclear", nuclear.value, "#00FF00");
            energyMix.addComponent("other", other.value, "#444444");
            energyMix.addComponent("waste", waste.value, "#444444");
            LOG.info("Energy mix is now: {}", energyMix);
        } catch (IOException e) {
            LOG.warn("Caught IOException", e);
        }

        // schedule next download, with optimum determined at 6 minutes after the hour
        Instant next = now.truncatedTo(ChronoUnit.HOURS).plus(Duration.ofMinutes(6));
        Duration delay = Duration.between(Instant.now(), next).truncatedTo(ChronoUnit.SECONDS);
        while (delay.isNegative()) {
            delay = delay.plus(ENTSO_INTERVAL);
            next = next.plus(ENTSO_INTERVAL);
        }
        LOG.info("Schedule next download after {}, at {}", delay, next);
        executor.schedule(new CatchingRunnable(this::downloadFromEntsoe), delay.getSeconds(), TimeUnit.SECONDS);
    }

    private Result sumGeneration(EntsoeParser parser, EPsrType... types) {
        Instant time = Instant.now().minus(Duration.ofDays(1));
        double value = 0.0;
        for (EPsrType type : types) {
            Result result = parser.findMostRecentGeneration(type);
            if (result.time.isAfter(time)) {
                time = result.time;
            }
            if (Double.isFinite(result.value)) {
                value += result.value;
            }
        }
        return new Result(time, value);
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
            // run on executor to avoid race condition with update
            return executor.submit(() -> energyMix).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Caught exception handling request", e);
            return null;
        }
    }

}
