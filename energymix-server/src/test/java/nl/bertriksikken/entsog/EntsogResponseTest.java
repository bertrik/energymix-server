package nl.bertriksikken.entsog;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.bertriksikken.entsog.EntsogAggregatedData.AggregatedData;

public final class EntsogResponseTest {

    @Test
    public void testSerialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        URL url = getClass().getClassLoader().getResource("entsog_aggregated_physical_flow.json");
        EntsogAggregatedData data = mapper.readValue(url, EntsogAggregatedData.class);
        Assert.assertEquals(13, data.aggregatedData.size());

        AggregatedData first = data.aggregatedData.get(0);
        Assert.assertEquals("21X-NL-A-A0A0A-Z", first.tsoEic);
        Assert.assertEquals("kWh/d", first.unit);
        Assert.assertEquals(6.711358333E8, first.value, 1);
    }

}
