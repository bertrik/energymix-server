package nl.bertriksikken.energymix.app;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Preconditions;

import nl.bertriksikken.energymix.server.EnergyMixHandler;

public final class ElectricityResourceHealthCheck extends HealthCheck {

    private final EnergyMixHandler handler;

    ElectricityResourceHealthCheck(EnergyMixHandler resource) {
        this.handler = Preconditions.checkNotNull(resource);
    }

    @Override
    protected Result check() throws Exception {
        if (!handler.isHealthy()) {
            return Result.unhealthy("not feeling well");
        }
        return Result.healthy();
    }

}
