package nl.bertriksikken.theice;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RunIceClient {

    private static final Logger LOG = LoggerFactory.getLogger(RunIceClient.class);

    public static void main(String[] args) throws IOException {
        RunIceClient runner = new RunIceClient();
        runner.run();
    }

    private void run() throws IOException {
        IceConfig config = new IceConfig();
        IceClient client = IceClient.create(config);

        List<Contract> contracts = client.getContracts();
        LOG.info("Contracts: {}", contracts);
    }

}
