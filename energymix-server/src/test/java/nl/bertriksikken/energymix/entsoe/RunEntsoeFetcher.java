package nl.bertriksikken.energymix.entsoe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Charsets;

import nl.bertriksikken.entsoe.EArea;
import nl.bertriksikken.entsoe.EDocumentType;
import nl.bertriksikken.entsoe.EProcessType;
import nl.bertriksikken.entsoe.EPsrType;
import nl.bertriksikken.entsoe.EntsoeRequest;

public final class RunEntsoeFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(RunEntsoeFetcher.class);
    private static final EArea AREA = EArea.NETHERLANDS;

    public static void main(String[] args) throws IOException {
        LOG.info("Start fetching data");

        YAMLMapper yamlMapper = new YAMLMapper();
        yamlMapper.findAndRegisterModules();
        EntsoeFetcherConfig config = yamlMapper.readValue(new File("entsoe.yaml"), EntsoeFetcherConfig.class);

        XmlMapper xmlMapper = new XmlMapper();
        EntsoeFetcher fetcher = EntsoeFetcher.create(config, xmlMapper);

        RunEntsoeFetcher test = new RunEntsoeFetcher();
        test.fetchActualGeneration(fetcher, "A75_actualgeneration.xml");
        test.fetchSolarForecast(fetcher, "A69_solar_wind_forecast.xml");
        test.fetchDayAheadPrices(fetcher, "A44_day_ahead_prices.xml");

        LOG.info("Done fetching data");
    }

    /**
     * https://transparency.entsoe.eu/generation/r2/dayAheadGenerationForecastWindAndSolar/show?name=
     * &defaultValue=false&viewType=GRAPH&areaType=CTY&atch=false&dateTime.dateTime=11.07.2022+00:00|CET|DAYTIMERANGE
     * &dateTime.endDateTime=11.07.2022+00:00|CET|DAYTIMERANGE&area.values=CTY|10YNL----------L!CTY|10YNL----------L
     * &productionType.values=B16&processType.values=A01&dateTime.timezone=CET_CEST
     * &dateTime.timezone_input=CET+(UTC+1)+/+CEST+(UTC+2)
     */
    private void fetchSolarForecast(EntsoeFetcher fetcher, String fileName) throws IOException {
        Instant now = Instant.now();
        Instant periodStart = now.truncatedTo(ChronoUnit.DAYS);
        Instant periodEnd = periodStart.plus(Duration.ofDays(1));

        EntsoeRequest request = new EntsoeRequest(EDocumentType.WIND_SOLAR_FORECAST);
        request.setProcessType(EProcessType.DAY_AHEAD);
        request.setInDomain(AREA);
        request.setPeriod(periodStart, periodEnd);
        request.setProductionType(EPsrType.SOLAR);
        String xml = fetcher.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), Charsets.UTF_8)) {
            writer.write(xml);
        }
    }

    private void fetchActualGeneration(EntsoeFetcher fetcher, String fileName) throws IOException {
        Instant now = Instant.now();
        Instant periodStart = now.truncatedTo(ChronoUnit.DAYS);
        Instant periodEnd = periodStart.plus(Duration.ofDays(1));

        EntsoeRequest request = new EntsoeRequest(EDocumentType.ACTUAL_GENERATION_PER_TYPE);
        request.setProcessType(EProcessType.REALISED);
        request.setInDomain(AREA);
        request.setPeriod(periodStart, periodEnd);
        String xml = fetcher.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), Charsets.UTF_8)) {
            writer.write(xml);
        }
    }

    private void fetchDayAheadPrices(EntsoeFetcher fetcher, String fileName) throws IOException {
        Instant now = Instant.now();
        Instant periodStart = now.truncatedTo(ChronoUnit.DAYS);
        Instant periodEnd = periodStart.plus(Duration.ofDays(1));

        EntsoeRequest request = new EntsoeRequest(EDocumentType.PRICE_DOCUMENT);
        EArea area = EArea.NETHERLANDS;
        request.setInDomain(area);
        request.setOutDomain(area);
        request.setPeriod(periodStart, periodEnd);
        String xml = fetcher.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), Charsets.UTF_8)) {
            writer.write(xml);
        }
    }
}