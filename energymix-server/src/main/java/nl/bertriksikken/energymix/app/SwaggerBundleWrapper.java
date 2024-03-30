package nl.bertriksikken.energymix.app;

import io.dropwizard.core.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SwaggerBundleWrapper extends SwaggerBundle<Configuration> {
    private final SwaggerBundleConfiguration swaggerBundleConfiguration = new SwaggerBundleConfiguration();

    SwaggerBundleWrapper(Class... resourceClasses) {
        String packages = Stream.of(resourceClasses)
                .map(c -> c.getPackage().getName()).collect(Collectors.joining(","));
        swaggerBundleConfiguration.setResourcePackage(packages);
    }

    @Override
    protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(Configuration configuration) {
        return swaggerBundleConfiguration;
    }
}
