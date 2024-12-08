package nl.bertriksikken.theice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.dropwizard.util.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

public final class ContractsTest {

    private static final Logger LOG = LoggerFactory.getLogger(ContractsTest.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testDeserialize() throws IOException {
        URL url = Resources.getResource("Contracts.json");
        List<Contract> contracts = OBJECT_MAPPER.readValue(url, new TypeReference<List<Contract>>() {
        });
        Assertions.assertNotNull(contracts);

        Contract contract = Iterables.getFirst(contracts, null);
        LOG.info("contact: {}", contract);
        Instant lastTime = contract.getLastTime();

        Assertions.assertEquals(110.395, contract.lastPrice, 0.001);
        Assertions.assertEquals(5460494, contract.marketId);
        Assertions.assertEquals("Dec22", contract.marketStrip);
        Assertions.assertEquals("2022-11-09T16:59:00Z", lastTime.toString());
    }

}
