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
import nl.bertriksikken.entsog.EntsogAggregatedData;
import nl.bertriksikken.entsog.EntsogRequest;
import nl.bertriksikken.naturalgas.GasFlows;
import nl.bertriksikken.naturalgas.GasFlowsFactory;

public final class RunEntsogClient {

    private static final Logger LOG = LoggerFactory.getLogger(RunEntsoeClient.class);
    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");

    public static void main(String[] args) throws IOException {
        RunEntsogClient test = new RunEntsogClient();
        EntsogClientConfig config = new EntsogClientConfig();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EntsogClient client = EntsogClient.create(config, objectMapper);

        String json = test.fetchFlows(client, "entsog_aggregated_physical_flow.json");
        LOG.info("done");

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        EntsogAggregatedData aggregatedData = mapper.readValue(json, EntsogAggregatedData.class);
        GasFlowsFactory factory = new GasFlowsFactory(ZONE);
        GasFlows gasFlows = factory.build(aggregatedData);
        String gasFlowsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(gasFlows);
        LOG.info("{}", gasFlowsJson);
    }

    private String fetchFlows(EntsogClient client, String fileName) throws IOException {
        EntsogRequest request = new EntsogRequest(EOperatorKey.GTS, EIndicator.PHYSICAL_FLOW);
        ZonedDateTime endTime = ZonedDateTime.now(ZONE).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime startTime = endTime.minusDays(1);
        request.setPeriod(ZONE, EPeriodType.DAY, startTime.toInstant(), endTime.toInstant());
        String json = client.getRawDocument(request.getParams());
        try (Writer writer = new FileWriter(new File(fileName), StandardCharsets.UTF_8)) {
            writer.write(json);
        }
        return json;
    }

}
