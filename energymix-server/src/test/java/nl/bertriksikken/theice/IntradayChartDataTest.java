package nl.bertriksikken.theice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.util.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public final class IntradayChartDataTest {

    private static final Logger LOG = LoggerFactory.getLogger(IntradayChartDataTest.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testDeserialize() throws IOException {
        URL url = Resources.getResource("IntradayChartData.json");
        IntradayChartData data = OBJECT_MAPPER.readValue(url, IntradayChartData.class);
        Assertions.assertNotNull(data);
        LOG.info("data: {}", data);
    }

}
