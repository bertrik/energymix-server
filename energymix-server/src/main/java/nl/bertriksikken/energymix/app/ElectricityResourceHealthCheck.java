package nl.bertriksikken.energymix.app;

import java.util.Objects;

import com.codahale.metrics.health.HealthCheck;

import nl.bertriksikken.energymix.server.ElectricityHandler;

public final class ElectricityResourceHealthCheck extends HealthCheck {

    private final ElectricityHandler handler;

    ElectricityResourceHealthCheck(ElectricityHandler resource) {
        this.handler = Objects.requireNonNull(resource);
    }

    @Override
    protected Result check() throws Exception {
        if (!handler.isHealthy()) {
            return Result.unhealthy("not feeling well");
        }
        return Result.healthy();
    }

}
