package nl.bertriksikken.energymix.server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import nl.bertriksikken.energymix.entsoe.EntsoeFetcher;
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
    private final EnergyMixConfig config;
    private final LoadingCache<DocumentKey, EntsoeResponse> documentCache;
    private final AtomicBoolean isHealthy = new AtomicBoolean(false);

    private EnergyMix energyMix;

    public EnergyMixHandler(EntsoeFetcher entsoeFetcher, EnergyMixConfig config) {
        this.entsoeFetcher = Preconditions.checkNotNull(entsoeFetcher);
        this.config = Preconditions.checkNotNull(config);
        this.documentCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofDays(1))
                .removalListener(this::onDocumentExpiry).build(CacheLoader.from(this::loadDocument));
    }

    private void onDocumentExpiry(RemovalNotification<DocumentKey, EntsoeResponse> notification) {
        LOG.info("Document {} expired for {}", notification.getValue().type, notification.getKey().dateTime);
    }

    // loads a document into the document cache
    private EntsoeResponse loadDocument(DocumentKey key) {
        try {
            Instant periodStart = key.dateTime.truncatedTo(ChronoUnit.DAYS).toInstant();
            Instant periodEnd = periodStart.plus(Duration.ofDays(1));
            switch (key.documentType) {
            case PRICE_DOCUMENT:
                return downloadPriceDocument(periodStart, periodEnd);
            case WIND_SOLAR_FORECAST:
                return downloadWindSolarForecast(periodStart, periodEnd);
            default:
                break;
            }
        } catch (IOException e) {
            LOG.warn("Caught IOException", e);
        }
        return new EntsoeResponse();
    }

    public void start() {
        LOG.info("Starting");
        executor.execute(new CatchingRunnable(LOG, this::updateEnergyMix));
    }

    // runs on the executor
    private void updateEnergyMix() {
        ZonedDateTime now = ZonedDateTime.now(config.getTimeZone());
        Instant periodStart = now.minusHours(2).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant periodEnd = now.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant();
        try {
            EntsoeResponse actualGenerationResponse = downloadGenerationByType(periodStart, periodEnd);
            EntsoeParser actualGenerationParser = new EntsoeParser(actualGenerationResponse);
            Result fossil = sumGeneration(actualGenerationParser, EPsrType.FOSSIL_HARD_COAL, EPsrType.FOSSIL_GAS);
            Result nuclear = sumGeneration(actualGenerationParser, EPsrType.NUCLEAR);
            Result windOffshoreReported = sumGeneration(actualGenerationParser, EPsrType.WIND_OFFSHORE);
            Result windOnshoreReported = sumGeneration(actualGenerationParser, EPsrType.WIND_ONSHORE);
            Result other = sumGeneration(actualGenerationParser, EPsrType.OTHER);
            Result waste = sumGeneration(actualGenerationParser, EPsrType.WASTE);
            ZonedDateTime fossilTime = ZonedDateTime.ofInstant(fossil.timeBegin, config.getTimeZone());
            LOG.info("Fossil generation: {}, age {}", fossil, Duration.between(fossil.timeEnd, now));

            // get solar/wind forecast
            ZonedDateTime forecastTime = fossilTime.plus(config.getForecastOffset());
            EntsoeResponse windSolarForecast = documentCache
                    .get(new DocumentKey(EDocumentType.WIND_SOLAR_FORECAST, forecastTime.truncatedTo(ChronoUnit.DAYS)));
            EntsoeParser windSolarParser = new EntsoeParser(windSolarForecast);
            Result solarForecast = windSolarParser.findByTime(forecastTime.toInstant(), EPsrType.SOLAR);
            LOG.info("Solar forecast: {}", solarForecast);
            Result windOffshoreForecast = windSolarParser.findByTime(forecastTime.toInstant(), EPsrType.WIND_OFFSHORE);
            Result windOnshoreForecast = windSolarParser.findByTime(forecastTime.toInstant(), EPsrType.WIND_ONSHORE);
            LOG.info("Wind forecast: {} (off-shore) + {} (on-shore) = {} (total)", windOffshoreForecast.value,
                    windOnshoreForecast.value, windOffshoreForecast.value + windOnshoreForecast.value);
            LOG.info("Wind reported: {} (off-shore) + {} (on-shore) = {} (total)", windOffshoreReported.value,
                    windOnshoreReported.value, windOffshoreReported.value + windOnshoreReported.value);

            // calculate wind
            Double windOnshore = Math.max(windOnshoreReported.value, windOnshoreForecast.value);
            Double wind = windOffshoreReported.value + windOnshore;

            // build energy mix structure
            energyMix = new EnergyMix(fossil.timeEnd.getEpochSecond());
            energyMix.addComponent("solar", solarForecast.value, "#FFFF00");
            energyMix.addComponent("wind", wind, "#0000FF");
            energyMix.addComponent("fossil", fossil.value, "#FF0000");
            energyMix.addComponent("nuclear", nuclear.value, "#00FF00");
            energyMix.addComponent("waste", waste.value, "#FF00FF");
            energyMix.addComponent("other", other.value, "#FF00FF");
            LOG.info("Energy mix is now: {}", energyMix);

            isHealthy.set(true);
        } catch (IOException | ExecutionException e) {
            LOG.warn("Caught Exception", e);
            isHealthy.set(false);
        }

        // schedule next download, with optimum determined at 6 minutes after the hour
        Instant next = now.truncatedTo(ChronoUnit.HOURS).plus(Duration.ofMinutes(6)).toInstant();
        Duration delay = Duration.between(Instant.now(), next);
        while (delay.isNegative()) {
            delay = delay.plus(ENTSO_INTERVAL);
            next = next.plus(ENTSO_INTERVAL);
        }
        LOG.info("Schedule next actual generation download after {}, at {}", delay, next);
        executor.schedule(new CatchingRunnable(LOG, this::updateEnergyMix), delay.toSeconds() + 1, TimeUnit.SECONDS);
    }

    private Result sumGeneration(EntsoeParser parser, EPsrType... types) {
        Instant timeBegin = Instant.now().minus(Duration.ofDays(1));
        Instant timeEnd = timeBegin;
        double value = 0.0;
        for (EPsrType type : types) {
            Result result = parser.findMostRecentGeneration(type);
            if (result != null) {
                if (result.timeBegin.isAfter(timeBegin)) {
                    timeBegin = result.timeBegin;
                }
                if (result.timeEnd.isAfter(timeEnd)) {
                    timeEnd = result.timeEnd;
                }
                if (Double.isFinite(result.value)) {
                    value += result.value;
                }
            }
        }
        return new Result(timeBegin, timeEnd, value);
    }

    private EntsoeResponse downloadGenerationByType(Instant periodStart, Instant periodEnd) throws IOException {
        LOG.info("Downloading actual generation per type");
        EntsoeRequest actualGenerationRequest = new EntsoeRequest(EDocumentType.ACTUAL_GENERATION_PER_TYPE);
        actualGenerationRequest.setProcessType(EProcessType.REALISED);
        actualGenerationRequest.setInDomain(config.getArea());
        actualGenerationRequest.setPeriod(periodStart, periodEnd);
        return entsoeFetcher.getDocument(actualGenerationRequest);
    }

    private EntsoeResponse downloadPriceDocument(Instant periodStart, Instant periodEnd) throws IOException {
        LOG.info("Downloading day-ahead prices");
        EntsoeRequest request = new EntsoeRequest(EDocumentType.PRICE_DOCUMENT);
        request.setInDomain(config.getArea());
        request.setOutDomain(config.getArea());
        request.setPeriod(periodStart, periodEnd);
        return entsoeFetcher.getDocument(request);
    }

    private EntsoeResponse downloadWindSolarForecast(Instant periodStart, Instant periodEnd) throws IOException {
        LOG.info("Downloading wind/solar forecast {}-{}", periodStart, periodEnd);
        EntsoeRequest request = new EntsoeRequest(EDocumentType.WIND_SOLAR_FORECAST);
        request.setProcessType(EProcessType.DAY_AHEAD);
        request.setInDomain(config.getArea());
        request.setPeriod(periodStart, periodEnd);
        return entsoeFetcher.getDocument(request);
    }

    public void stop() throws InterruptedException {
        LOG.info("Stopping");
        executor.shutdownNow();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * @return a structure containing the latest recently known mix
     */
    public EnergyMix getGeneration() {
        try {
            // run on executor to avoid race condition with update
            return executor.submit(() -> energyMix).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Caught exception handling request", e);
            return null;
        }
    }

    /**
     * @return a structure containing the day-ahead electricity prices
     */
    public DayAheadPrices getPrices() {
        ZonedDateTime now = ZonedDateTime.now(config.getTimeZone());
        try {
            // get the day-ahead price document
            DocumentKey key = new DocumentKey(EDocumentType.PRICE_DOCUMENT, now.truncatedTo(ChronoUnit.DAYS));
            EntsoeResponse priceDocument = documentCache.get(key);
            // extract data
            EntsoeParser parser = new EntsoeParser(priceDocument);
            List<Result> results = parser.parseDayAheadPrices();
            double currentPrice = parser.findDayAheadPrice(now.toInstant());
            // build response structure
            DayAheadPrices prices = new DayAheadPrices(now.toInstant(), currentPrice);
            results.forEach(r -> prices.addPrice(r.timeBegin, r.value));
            return prices;
        } catch (ExecutionException e) {
            LOG.error("Caught exception handling request", e);
            return null;
        }
    }

    public boolean isHealthy() {
        return isHealthy.get();
    }

    // document/time combination, used in the dynamic cache
    private static final class DocumentKey {
        private final EDocumentType documentType;
        private final ZonedDateTime dateTime;

        private DocumentKey(EDocumentType documentType, ZonedDateTime dateTime) {
            this.documentType = documentType;
            this.dateTime = dateTime;
        }

        @Override
        public int hashCode() {
            return Objects.hash(documentType, dateTime);
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof DocumentKey) {
                DocumentKey other = (DocumentKey) object;
                return documentType.equals(other.documentType) && dateTime.equals(other.dateTime);
            }
            return false;
        }
    }

}
