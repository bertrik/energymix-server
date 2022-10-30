package nl.bertriksikken.energymix.app;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import es.moki.ratelimij.dropwizard.RateLimitBundle;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import es.moki.ratelimitj.inmemory.InMemoryRateLimiterFactory;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.bertriksikken.energymix.entsoe.EntsoeFetcher;
import nl.bertriksikken.energymix.server.EnergyMixHandler;
import nl.bertriksikken.energymix.server.NaturalGasHandler;

public final class EnergyMixApp extends Application<EnergyMixAppConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(EnergyMixApp.class);
    private static final String CONFIG_FILE = "configuration.yaml";

    private EnergyMixApp() {
    }

    @Override
    public void initialize(Bootstrap<EnergyMixAppConfig> bootstrap) {
        RequestRateLimiterFactory factory = new InMemoryRateLimiterFactory();
        bootstrap.addBundle(new RateLimitBundle(factory));
    }

    @Override
    public void run(EnergyMixAppConfig configuration, Environment environment) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        EntsoeFetcher entsoeFetcher = EntsoeFetcher.create(configuration.entsoeFetcherConfig, xmlMapper);
        EnergyMixHandler handler = new EnergyMixHandler(entsoeFetcher, configuration.energyMixConfig);
        ElectricityResource electricityResource = new ElectricityResource(handler);
        EnergyMixResource energyMixResource = new EnergyMixResource(electricityResource);
        environment.healthChecks().register("electricity", new ElectricityResourceHealthCheck(handler));
        environment.jersey().register(electricityResource);
        environment.jersey().register(energyMixResource);
        environment.lifecycle().manage(electricityResource);

        // natural gas
        NaturalGasHandler naturalGasHandler = new NaturalGasHandler(configuration.powerNextConfig);
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
