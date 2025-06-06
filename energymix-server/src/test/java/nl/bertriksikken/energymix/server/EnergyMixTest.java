package nl.bertriksikken.energymix.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

public final class EnergyMixTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");

    @Test
    public void testSerialization() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Instant time = Instant.ofEpochSecond(1656750073);
        EnergyMixFactory factory = new EnergyMixFactory(ZONE);
        EnergyMix mix = factory.build(time);
        mix.addComponent("solar", 30, "#FFFF00");

        String json = mapper.writeValueAsString(mix);
        Assertions.assertEquals("{\"time\":1656750073,\"datetime\":\"2022-07-02T10:21:13+02:00\",\"total\":30,"
                + "\"mix\":[{\"id\":\"solar\",\"power\":30,\"color\":\"#FFFF00\"}]}", json);
        Assertions.assertNotNull(mix.toString());
    }

}
