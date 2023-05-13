package nl.bertriksikken.energymix.app;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import nl.bertriksikken.eex.EexClient;
import nl.bertriksikken.energymix.entsoe.EntsoeClient;
import nl.bertriksikken.energymix.entsog.EntsogClient;
import nl.bertriksikken.energymix.server.ElectricityHandler;
import nl.bertriksikken.energymix.server.NaturalGasHandler;
import nl.bertriksikken.theice.IceClient;

public final class EnergyMixApp extends Application<EnergyMixAppConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(EnergyMixApp.class);
    private static final String CONFIG_FILE = "configuration.yaml";

    private EnergyMixApp() {
    }

    @Override
    public void initialize(Bootstrap<EnergyMixAppConfig> bootstrap) {
        bootstrap.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        bootstrap.addBundle(new AssetsBundle("/assets/energy.png", "/favicon.ico"));
    }

    @Override
    public void run(EnergyMixAppConfig configuration, Environment environment) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();

        EntsoeClient entsoeFetcher = EntsoeClient.create(configuration.entsoeFetcherConfig, xmlMapper);
        ElectricityHandler handler = new ElectricityHandler(entsoeFetcher, configuration.energyMixConfig);
        ElectricityResource electricityResource = new ElectricityResource(handler);
        environment.healthChecks().register("electricity", new ElectricityResourceHealthCheck(handler));
        environment.jersey().register(electricityResource);
        environment.lifecycle().manage(electricityResource);

        // natural gas
        EexClient eexClient = EexClient.create(configuration.eexConfig);
        IceClient iceClient = IceClient.create(configuration.iceConfig);
        EntsogClient entsogClient = EntsogClient.create(configuration.entsogConfig);
        NaturalGasHandler naturalGasHandler = new NaturalGasHandler(eexClient, iceClient, entsogClient);
        NaturalGasResource naturalGasResource = new NaturalGasResource(naturalGasHandler);
        environment.jersey().register(naturalGasResource);
        environment.lifecycle().manage(naturalGasResource);
    }

    public static void main(String[] args) throws Exception {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            LOG.info("Config file not found, creating default");
            YAMLMapper mapper = new YAMLMapper();
            EnergyMixAppConfig config = new EnergyMixAppConfig();
            mapper.writeValue(configFile, config);
        }

        EnergyMixApp app = new EnergyMixApp();
        app.run("server", CONFIG_FILE);
    }

}
