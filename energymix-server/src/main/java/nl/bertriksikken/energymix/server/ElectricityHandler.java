package nl.bertriksikken.energymix.server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import nl.bertriksikken.energymix.entsoe.EntsoeClient;
import nl.bertriksikken.energymix.entsoe.EntsoeConfig;
import nl.bertriksikken.entsoe.EDocumentType;
import nl.bertriksikken.entsoe.EProcessType;
import nl.bertriksikken.entsoe.EPsrType;
import nl.bertriksikken.entsoe.EntsoeParser;
import nl.bertriksikken.entsoe.EntsoeParser.Result;
import nl.bertriksikken.entsoe.EntsoeRequest;
import nl.bertriksikken.entsoe.EntsoeResponse;

public final class ElectricityHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ElectricityHandler.class);
    private static final Duration ENTSO_INTERVAL = Duration.ofMinutes(15);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final EntsoeClient entsoeClient;
    private final EntsoeConfig entsoeConfig;
    private final LoadingCache<DocumentKey, EntsoeResponse> documentCache;
    private final AtomicBoolean isHealthy = new AtomicBoolean(false);
    private final EnergyMixFactory energyMixFactory;

    private EnergyMix energyMix;

    public ElectricityHandler(EntsoeConfig config) {
        this.entsoeClient = EntsoeClient.create(config);
        this.entsoeConfig = Objects.requireNonNull(config);
        this.documentCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofDays(1))
                .removalListener(this::logDocumentExpiry).build(CacheLoader.from(this::loadDocument));
        this.energyMixFactory = new EnergyMixFactory(config.getTimeZone());
    }

    private void logDocumentExpiry(RemovalNotification<DocumentKey, EntsoeResponse> notification) {
        LOG.info("Document {} expired for {}", notification.getValue().type, notification.getKey().dateTime);
        LOG.info("Cache stats: {}", documentCache.stats());
    }

    // loads a document into the document cache
    private EntsoeResponse loadDocument(DocumentKey key) {
        try {
            ZonedDateTime periodStart = key.dateTime.truncatedTo(ChronoUnit.DAYS);
            ZonedDateTime periodEnd;
            switch (key.documentType) {
            case PRICE_DOCUMENT:
                periodEnd = periodStart.plus(Duration.ofDays(2));
                return downloadPriceDocument(periodStart.toInstant(), periodEnd.toInstant());
            case INSTALLED_CAPACITY_PER_TYPE:
                ZonedDateTime capacityStart = periodStart.withDayOfYear(1);
                ZonedDateTime capacityEnd = capacityStart.plusYears(1);
                return downloadInstalledCapacity(capacityStart.toInstant(), capacityEnd.toInstant());
            case WIND_SOLAR_FORECAST:
                periodEnd = periodStart.plus(Duration.ofDays(1));
                return downloadWindSolarForecast(periodStart.toInstant(), periodEnd.toInstant());
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
        ZonedDateTime now = ZonedDateTime.now(entsoeConfig.getTimeZone());
        ZonedDateTime today = now.truncatedTo(ChronoUnit.DAYS);
        Instant periodStart = now.minusHours(2).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant periodEnd = today.plusDays(1).toInstant();
        try {
            // get generation by production type
            EntsoeResponse actualGenerationResponse = downloadGenerationByType(periodStart, periodEnd);
            EntsoeParser actualGenerationParser = new EntsoeParser(actualGenerationResponse);
            Result fossilCoal = sumGeneration(actualGenerationParser, EPsrType.FOSSIL_HARD_COAL);
            Result fossilGas = sumGeneration(actualGenerationParser, EPsrType.FOSSIL_GAS);
            Result nuclear = sumGeneration(actualGenerationParser, EPsrType.NUCLEAR);
            Result windOffshoreReported = sumGeneration(actualGenerationParser, EPsrType.WIND_OFFSHORE);
            Result windOnshoreReported = sumGeneration(actualGenerationParser, EPsrType.WIND_ONSHORE);
            Result other = sumGeneration(actualGenerationParser, EPsrType.OTHER);
            Result waste = sumGeneration(actualGenerationParser, EPsrType.WASTE);
            ZonedDateTime fossilTime = ZonedDateTime.ofInstant(fossilGas.timeBegin, entsoeConfig.getTimeZone());
            LOG.info("Fossil gas generation: {}, age {}", fossilGas, Duration.between(fossilGas.timeEnd, now));

            // get solar/wind forecast
            ZonedDateTime forecastTime = fossilTime.plus(entsoeConfig.getForecastOffset());
            DocumentKey forecastKey = new DocumentKey(EDocumentType.WIND_SOLAR_FORECAST,
                    forecastTime.truncatedTo(ChronoUnit.DAYS));
            EntsoeResponse windSolarForecast = documentCache.get(forecastKey);
            EntsoeParser windSolarParser = new EntsoeParser(windSolarForecast);
            Result solarForecast = windSolarParser.findByTime(forecastTime.toInstant(), EPsrType.SOLAR);
            LOG.info("Solar forecast: {}", solarForecast);
            Result windOffshoreForecast = windSolarParser.findByTime(forecastTime.toInstant(), EPsrType.WIND_OFFSHORE);
            Result windOnshoreForecast = windSolarParser.findByTime(forecastTime.toInstant(), EPsrType.WIND_ONSHORE);
            LOG.info("Wind forecast: {} (off-shore) + {} (on-shore) = {} (total)", windOffshoreForecast.value,
                    windOnshoreForecast.value, windOffshoreForecast.value + windOnshoreForecast.value);
            LOG.info("Wind reported: {} (off-shore) + {} (on-shore) = {} (total)", windOffshoreReported.value,
                    windOnshoreReported.value, windOffshoreReported.value + windOnshoreReported.value);
            if (!Double.isFinite(solarForecast.value) || !Double.isFinite(windOnshoreForecast.value)) {
                // invalidate to trigger new download next time
                LOG.info("Invalidating forecast: {}", forecastKey);
                documentCache.invalidate(forecastKey);
            }

            // calculate wind
            Double windOnshore = Math.max(windOnshoreReported.value, windOnshoreForecast.value);
            Double windOffshore = windOffshoreReported.value;

            // build energy mix structure
            energyMix = energyMixFactory.build(fossilGas.timeEnd);
            energyMix.addComponent("solar", solarForecast.value, "#FFFF00");
            energyMix.addComponent("wind onshore", windOnshore, "#0000FF");
            energyMix.addComponent("wind offshore", windOffshore, "#0000FF");
            energyMix.addComponent("fossil gas", fossilGas.value, "#FF0000");
            energyMix.addComponent("fossil coal", fossilCoal.value, "#FF0000");
            energyMix.addComponent("nuclear", nuclear.value, "#00FF00");
            energyMix.addComponent("waste", waste.value, "#FF00FF");
            energyMix.addComponent("other", other.value, "#FF00FF");
            LOG.info("ENTSO-E mix: {}", energyMix);

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
        actualGenerationRequest.setInDomain(entsoeConfig.getArea());
        actualGenerationRequest.setPeriod(periodStart, periodEnd);
        return entsoeClient.getDocument(actualGenerationRequest);
    }

    private EntsoeResponse downloadPriceDocument(Instant periodStart, Instant periodEnd) throws IOException {
        LOG.info("Downloading day-ahead prices");
        EntsoeRequest request = new EntsoeRequest(EDocumentType.PRICE_DOCUMENT);
        request.setInDomain(entsoeConfig.getArea());
        request.setOutDomain(entsoeConfig.getArea());
        request.setPeriod(periodStart, periodEnd);
        return entsoeClient.getDocument(request);
    }

    private EntsoeResponse downloadWindSolarForecast(Instant periodStart, Instant periodEnd) throws IOException {
        LOG.info("Downloading wind/solar forecast {}-{}", periodStart, periodEnd);
        EntsoeRequest request = new EntsoeRequest(EDocumentType.WIND_SOLAR_FORECAST);
        request.setProcessType(EProcessType.DAY_AHEAD);
        request.setInDomain(entsoeConfig.getArea());
        request.setPeriod(periodStart, periodEnd);
        return entsoeClient.getDocument(request);
    }

    private EntsoeResponse downloadInstalledCapacity(Instant periodStart, Instant periodEnd) throws IOException {
        EntsoeRequest request = new EntsoeRequest(EDocumentType.INSTALLED_CAPACITY_PER_TYPE);
        request.setProcessType(EProcessType.YEAR_AHEAD);
        request.setInDomain(entsoeConfig.getArea());
        request.setPeriod(periodStart, periodEnd);
        return entsoeClient.getDocument(request);
    }

    public void stop() {
        LOG.info("Stopping");
        executor.shutdownNow();
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
        ZonedDateTime now = ZonedDateTime.now(entsoeConfig.getTimeZone());
        try {
            // get the day-ahead price document
            DocumentKey key = new DocumentKey(EDocumentType.PRICE_DOCUMENT, now.truncatedTo(ChronoUnit.HOURS));
            EntsoeResponse priceDocument = documentCache.get(key);
            // extract data
            EntsoeParser parser = new EntsoeParser(priceDocument);
            List<Result> results = parser.parseDayAheadPrices();
            double currentPrice = parser.findDayAheadPrice(now.toInstant());
            if (!Double.isFinite(currentPrice)) {
                // invalidate cache, so next request triggers a new download attempt
                documentCache.invalidate(key);
            }
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

    public GenerationCapacity getCapacity() {
        GenerationCapacity generationCapacity = new GenerationCapacity();

        ZonedDateTime now = ZonedDateTime.now(entsoeConfig.getTimeZone());
        ZonedDateTime today = now.truncatedTo(ChronoUnit.DAYS);

        // get installed capacity
        EntsoeResponse response;
        try {
            response = documentCache.get(new DocumentKey(EDocumentType.INSTALLED_CAPACITY_PER_TYPE, today));
        } catch (ExecutionException e) {
            LOG.warn("Caught Exception", e);
            isHealthy.set(false);
            return generationCapacity;
        }
        EntsoeParser capacityParser = new EntsoeParser(response);
        Map<EPsrType, Integer> capacities = capacityParser.parseInstalledCapacity();
        for (Entry<EPsrType, Integer> entry : capacities.entrySet()) {
            EPsrType type = entry.getKey();
            Integer power = entry.getValue();
            if ((power != null) && (power > 0)) {
                generationCapacity.add(type.getCode(), type.getDescription(), power);
            }
        }
        return generationCapacity;
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
            if (object instanceof DocumentKey other) {
                return documentType.equals(other.documentType) && dateTime.equals(other.dateTime);
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s for %s", documentType.getCode(), dateTime.toString());
        }
    }

}
