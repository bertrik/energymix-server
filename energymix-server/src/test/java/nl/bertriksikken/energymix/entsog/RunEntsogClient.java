package nl.bertriksikken.energymix.entsog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.bertriksikken.energymix.entsoe.RunEntsoeClient;
import nl.bertriksikken.entsog.EIndicator;
import nl.bertriksikken.entsog.EOperatorKey;
import nl.bertriksikken.entsog.EPeriodType;
import nl.bertriksikken.entsog.EntsogRequest;

public final class RunEntsogClient {

    private static final Logger LOG = LoggerFactory.getLogger(RunEntsoeClient.class);

    public static void main(String[] args) throws IOException {
        RunEntsogClient test = new RunEntsogClient();
        EntsogClientConfig config = new EntsogClientConfig();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EntsogClient client = EntsogClient.create(config, objectMapper);

        test.fetchFlows(client, "entsog_aggregated_physical_flow.json");
        LOG.info("done");
    }

    private void fetchFlows(EntsogClient client, String fileName) throws IOException {
        ZoneId zoneId = ZoneId.of("Europe/Amsterdam");
        EntsogRequest request = new EntsogRequest(EOperatorKey.GTS, EIndicator.PHYSICAL_FLOW);
        ZonedDateTime endTime = ZonedDateTime.now(zoneId).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime startTime = endTime.minusDays(1);
        request.setPeriod(zoneId, EPeriodType.DAY, startTime.toInstant(), endTime.toInstant());
        String json = client.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), StandardCharsets.UTF_8)) {
            writer.write(json);
        }
    }

}
