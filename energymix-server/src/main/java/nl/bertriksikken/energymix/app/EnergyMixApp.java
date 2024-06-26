package nl.bertriksikken.energymix.app;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import nl.bertriksikken.eex.EexClient;
import nl.bertriksikken.energymix.entsog.EntsogClient;
import nl.bertriksikken.energymix.ned.NedHandler;
import nl.bertriksikken.energymix.resource.ElectricityResource;
import nl.bertriksikken.energymix.resource.NaturalGasResource;
import nl.bertriksikken.energymix.server.ElectricityHandler;
import nl.bertriksikken.energymix.server.NaturalGasHandler;
import nl.bertriksikken.theice.IceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public final class EnergyMixApp extends Application<EnergyMixAppConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(EnergyMixApp.class);
    private static final String CONFIG_FILE = "configuration.yaml";

    private EnergyMixApp() {
    }

    @Override
    public void initialize(Bootstrap<EnergyMixAppConfig> bootstrap) {
        bootstrap.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        bootstrap.addBundle(new AssetsBundle("/assets/energy.png", "/favicon.ico"));
        bootstrap.addBundle(new SwaggerBundleWrapper(ElectricityResource.class, NaturalGasResource.class));
    }

    @Override
    public void run(EnergyMixAppConfig configuration, Environment environment) {
        ElectricityHandler entsoHandler = new ElectricityHandler(configuration.entsoeConfig);
        NedHandler nedHandler = new NedHandler(configuration.nedConfig);
        ElectricityResource electricityResource = new ElectricityResource(entsoHandler, nedHandler);
        environment.healthChecks().register("electricity", new ElectricityResourceHealthCheck(entsoHandler));
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

        // Add CORS header to each response
        environment.jersey().register((ContainerResponseFilter) this::addCorsHeader);
    }

    private void addCorsHeader(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
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
