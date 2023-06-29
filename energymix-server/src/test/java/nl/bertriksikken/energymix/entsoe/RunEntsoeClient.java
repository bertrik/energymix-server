package nl.bertriksikken.energymix.entsoe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import nl.bertriksikken.entsoe.EArea;
import nl.bertriksikken.entsoe.EDocumentType;
import nl.bertriksikken.entsoe.EProcessType;
import nl.bertriksikken.entsoe.EPsrType;
import nl.bertriksikken.entsoe.EntsoeRequest;

public final class RunEntsoeClient {

    private static final Logger LOG = LoggerFactory.getLogger(RunEntsoeClient.class);
    private static final EArea AREA = EArea.NETHERLANDS;
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Amsterdam");

    public static void main(String[] args) throws IOException {
        LOG.info("Start fetching data");

        YAMLMapper yamlMapper = new YAMLMapper();
        yamlMapper.findAndRegisterModules();
        EntsoeClientConfig config = yamlMapper.readValue(new File("entsoe.yaml"), EntsoeClientConfig.class);

        EntsoeClient fetcher = EntsoeClient.create(config);

        RunEntsoeClient test = new RunEntsoeClient();
        test.fetchInstalledGeneration(fetcher, "A68_installed_capacity.xml");
        test.fetchActualGeneration(fetcher, "A75_actualgeneration.xml");
        test.fetchSolarForecast(fetcher, "A69_solar_wind_forecast.xml");
        test.fetchDayAheadPrices(fetcher, "A44_day_ahead_prices.xml");

        LOG.info("Done fetching data");
    }

    private void fetchInstalledGeneration(EntsoeClient client, String fileName) throws IOException {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime periodStart = now.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime periodEnd = periodStart.plusYears(1);

        EntsoeRequest request = new EntsoeRequest(EDocumentType.INSTALLED_CAPACITY_PER_TYPE);
        request.setProcessType(EProcessType.YEAR_AHEAD);
        request.setInDomain(AREA.getCode());
        request.setPeriod(periodStart.toInstant(), periodEnd.toInstant());
        String xml = client.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), StandardCharsets.UTF_8)) {
            writer.write(xml);
        }
    }

    /**
     * https://transparency.entsoe.eu/generation/r2/dayAheadGenerationForecastWindAndSolar/show?name=
     * &defaultValue=false&viewType=GRAPH&areaType=CTY&atch=false&dateTime.dateTime=11.07.2022+00:00|CET|DAYTIMERANGE
     * &dateTime.endDateTime=11.07.2022+00:00|CET|DAYTIMERANGE&area.values=CTY|10YNL----------L!CTY|10YNL----------L
     * &productionType.values=B16&processType.values=A01&dateTime.timezone=CET_CEST
     * &dateTime.timezone_input=CET+(UTC+1)+/+CEST+(UTC+2)
     */
    private void fetchSolarForecast(EntsoeClient fetcher, String fileName) throws IOException {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        Instant periodStart = now.truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant periodEnd = periodStart.plus(Duration.ofDays(1));

        EntsoeRequest request = new EntsoeRequest(EDocumentType.WIND_SOLAR_FORECAST);
        request.setProcessType(EProcessType.DAY_AHEAD);
        request.setInDomain(AREA.getCode());
        request.setPeriod(periodStart, periodEnd);
        request.setProductionType(EPsrType.SOLAR);
        String xml = fetcher.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), StandardCharsets.UTF_8)) {
            writer.write(xml);
        }
    }

    private void fetchActualGeneration(EntsoeClient fetcher, String fileName) throws IOException {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        Instant periodStart = now.minusHours(2).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant periodEnd = now.truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant();

        EntsoeRequest request = new EntsoeRequest(EDocumentType.ACTUAL_GENERATION_PER_TYPE);
        request.setProcessType(EProcessType.REALISED);
        request.setInDomain(AREA.getCode());
        request.setPeriod(periodStart, periodEnd);
        String xml = fetcher.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), StandardCharsets.UTF_8)) {
            writer.write(xml);
        }
    }

    private void fetchDayAheadPrices(EntsoeClient fetcher, String fileName) throws IOException {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        Instant periodStart = now.truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant periodEnd = periodStart.plus(Duration.ofDays(1));

        EntsoeRequest request = new EntsoeRequest(EDocumentType.PRICE_DOCUMENT);
        EArea area = EArea.NETHERLANDS;
        request.setInDomain(area.getCode());
        request.setOutDomain(area.getCode());
        request.setPeriod(periodStart, periodEnd);
        String xml = fetcher.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), StandardCharsets.UTF_8)) {
            writer.write(xml);
        }
    }
}