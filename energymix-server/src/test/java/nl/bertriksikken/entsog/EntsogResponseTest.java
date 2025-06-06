package nl.bertriksikken.entsog;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Iterables;

import nl.bertriksikken.entsog.EntsogAggregatedData.AggregatedData;
import nl.bertriksikken.naturalgas.GasFlows;
import nl.bertriksikken.naturalgas.GasFlowsFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class EntsogResponseTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Amsterdam");

    @Test
    public void testSerialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        URL url = getClass().getClassLoader().getResource("entsog_aggregated_physical_flow.json");
        EntsogAggregatedData aggregatedData = mapper.readValue(url, EntsogAggregatedData.class);
        Assertions.assertEquals(13, aggregatedData.aggregatedData.size());

        AggregatedData first = Iterables.getFirst(aggregatedData.aggregatedData, null);
        Assertions.assertEquals("21X-NL-A-A0A0A-Z", first.tsoEic);
        Assertions.assertEquals("kWh/d", first.unit);
        Assertions.assertEquals(6.711358333E8, first.value, 1);

        // build a nice structure
        GasFlowsFactory factory = new GasFlowsFactory(ZONE_ID);
        GasFlows gasFlows = factory.build(aggregatedData);
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        String json = writer.writeValueAsString(gasFlows);
        System.out.println(json);
    }

}
