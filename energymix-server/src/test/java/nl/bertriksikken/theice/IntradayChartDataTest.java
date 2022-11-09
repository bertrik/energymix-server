package nl.bertriksikken.theice;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.util.Resources;

public final class IntradayChartDataTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testDeserialize() throws StreamReadException, DatabindException, IOException {
        URL url = Resources.getResource("IntradayChartData.json");
        IntradayChartData data = OBJECT_MAPPER.readValue(url, IntradayChartData.class);
        Assert.assertNotNull(data);
    }

}
