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
import nl.bertriksikken.energymix.server.DayAheadPrices;
import nl.bertriksikken.energymix.server.EnergyMix;
import nl.bertriksikken.energymix.server.EnergyMixHandler;

/**
 * Main REST endpoint for (dutch) electricity queries.
 */
@Path("/electricity")
public class ElectricityResource implements Managed {

    private final EnergyMixHandler handler;

    ElectricityResource(EnergyMixHandler handler) {
        this.handler = Preconditions.checkNotNull(handler);
    }

    @Override
    public void start() {
        handler.start();
    }

    @Override
    public void stop() throws InterruptedException {
        handler.stop();
    }

    @GET
    @Path("/ping")
    public String ping() {
        return "pong!";
    }

    @GET
    @Path("/generation")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.MINUTES)
    @RateLimited(keys = KeyPart.ANY, rates = { @Rate(duration = 1, timeUnit = TimeUnit.MINUTES, limit = 10) })
    public EnergyMix getGeneration() {
        return handler.getGeneration();
    }

    @GET
    @Path("/price")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(maxAge = 60, maxAgeUnit = TimeUnit.MINUTES)
    @RateLimited(keys = KeyPart.ANY, rates = { @Rate(duration = 1, timeUnit = TimeUnit.MINUTES, limit = 10) })
    public DayAheadPrices getPrices() {
        return handler.getPrices();
    }

}
