package nl.bertriksikken.energymix.resource;

import io.dropwizard.jersey.caching.CacheControl;
import io.dropwizard.lifecycle.Managed;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import nl.bertriksikken.energymix.server.DayAheadPrices;
import nl.bertriksikken.energymix.server.ElectricityHandler;
import nl.bertriksikken.energymix.server.EnergyMix;
import nl.bertriksikken.energymix.server.GenerationCapacity;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Main REST endpoint for (dutch) electricity queries.
 */
@Path("/electricity")
public class ElectricityResource implements Managed, IEnergyResource {

    private final ElectricityHandler handler;

    public ElectricityResource(ElectricityHandler handler) {
        this.handler = Objects.requireNonNull(handler);
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
    @Operation(hidden = true)
    public String ping() {
        return "pong!";
    }

    @GET
    @Path("/generation")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.MINUTES)
    @Operation(description = "The electricity generation mix, by production type", tags = {"electricity"})
    public EnergyMix getGeneration() {
        return handler.getGeneration();
    }

    @GET
    @Path("/price")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(maxAge = 60, maxAgeUnit = TimeUnit.MINUTES)
    @Operation(description = "Electricity price, per hour", tags = {"electricity"})
    public DayAheadPrices getPrices() {
        return handler.getPrices();
    }

    @GET
    @Path("/capacity")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    @Operation(description = "Electricity generation capacity, by production type", tags = {"electricity"})
    public GenerationCapacity getCapacity() {
        return handler.getCapacity();
    }

}
