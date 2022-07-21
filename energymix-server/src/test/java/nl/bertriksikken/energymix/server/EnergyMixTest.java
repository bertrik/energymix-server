package nl.bertriksikken.energymix.server;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class EnergyMixTest {

    @Test
    public void testSerialization() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        EnergyMix mix = new EnergyMix(1656750073);
        mix.addComponent("solar", 30, "#FFFF00");

        String s = mapper.writeValueAsString(mix);
        Assert.assertEquals("{\"time\":1656750073,\"total\":30,"
                + "\"mix\":[{\"id\":\"solar\",\"power\":30,\"color\":\"#FFFF00\"}]}", s);
    }

}
