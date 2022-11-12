package nl.bertriksikken.theice;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;

import io.dropwizard.util.Resources;

public final class ContractsTest {

    private static final Logger LOG = LoggerFactory.getLogger(ContractsTest.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testDeserialize() throws StreamReadException, DatabindException, IOException {
        URL url = Resources.getResource("Contracts.json");
        List<Contract> contracts = OBJECT_MAPPER.readValue(url, new TypeReference<List<Contract>>() {
        });
        Assert.assertNotNull(contracts);

        Contract contract = Iterables.getFirst(contracts, null);
        LOG.info("contact: {}", contract);
        Instant lastTime = contract.getLastTime();

        Assert.assertEquals(110.395, contract.lastPrice, 0.001);
        Assert.assertEquals(5460494, contract.marketId);
        Assert.assertEquals("Dec22", contract.marketStrip);
        Assert.assertEquals("2022-11-09T16:59:00Z", lastTime.toString());
    }

}
