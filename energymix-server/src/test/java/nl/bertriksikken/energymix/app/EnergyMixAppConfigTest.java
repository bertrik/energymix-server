package nl.bertriksikken.energymix.app;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class EnergyMixAppConfigTest {

    @Test
    public void testWriteDefault() throws IOException {
        EnergyMixAppConfig config = new EnergyMixAppConfig();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("defaults.yaml"), config);
    }
    
}
