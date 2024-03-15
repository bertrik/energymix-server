package nl.bertriksikken.entsog;

import nl.bertriksikken.energymix.entsog.EntsogClient;
import nl.bertriksikken.energymix.entsog.EntsogClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public final class RunEntsogClient {

    private static final Logger LOG = LoggerFactory.getLogger(RunEntsogClient.class);

    private static final ZoneId ENTSOG_ZONE = ZoneId.of("Europe/Amsterdam");

    private RunEntsogClient() {
    }

    public static void main(String[] args) throws IOException {
        RunEntsogClient runner = new RunEntsogClient();
        runner.fetch();
    }

    private void fetch() throws IOException {
        EntsogClientConfig config = new EntsogClientConfig();
        EntsogClient client = EntsogClient.create(config);

        EntsogRequest request = new EntsogRequest(EOperatorKey.GTS, EIndicator.PHYSICAL_FLOW);
        ZonedDateTime endTime = ZonedDateTime.now(ENTSOG_ZONE).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime startTime = endTime.minusDays(1);
        request.setPeriod(ENTSOG_ZONE, EPeriodType.DAY, startTime.toInstant(), endTime.toInstant());

        EntsogAggregatedData data = client.getAggregatedData(request);
        LOG.info("message={}", data.message);
    }

}
