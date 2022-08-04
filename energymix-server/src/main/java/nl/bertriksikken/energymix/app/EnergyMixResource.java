package nl.bertriksikken.energymix.app;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Preconditions;

import es.moki.ratelimij.dropwizard.annotation.Rate;
import es.moki.ratelimij.dropwizard.annotation.RateLimited;
import es.moki.ratelimij.dropwizard.filter.KeyPart;
import io.dropwizard.jersey.caching.CacheControl;
import io.dropwizard.lifecycle.Managed;
import nl.bertriksikken.energymix.server.EnergyMix;

/**
 * Legacy REST endpoint for (dutch) electricity queries.
 */
@Path("/energy")
public class EnergyMixResource implements Managed {

    private final ElectricityResource electricityResource;

    EnergyMixResource(ElectricityResource electricityResource) {
        this.electricityResource = Preconditions.checkNotNull(electricityResource);
    }

    @GET
    @Path("/latest")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(maxAge = 15, maxAgeUnit = TimeUnit.MINUTES)
    @RateLimited(keys = KeyPart.ANY, rates = { @Rate(duration = 1, timeUnit = TimeUnit.MINUTES, limit = 2) })
    public EnergyMix getLatest() {
        return electricityResource.getGeneration();
    }

}
