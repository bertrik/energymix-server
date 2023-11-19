package nl.bertriksikken.energymix.app;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public final class EnergyMixAppConfigTest {

    @Test
    public void testWriteDefault() throws IOException {
        EnergyMixAppConfig config = new EnergyMixAppConfig();
        YAMLMapper mapper = new YAMLMapper();
        mapper.findAndRegisterModules();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("defaults.yaml"), config);
    }
    
}
