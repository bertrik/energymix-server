package nl.bertriksikken.energymix.app;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Preconditions;

import io.dropwizard.lifecycle.Managed;
import nl.bertriksikken.energymix.server.EnergyMix;
import nl.bertriksikken.energymix.server.EnergyMixHandler;

@Path("/energy")
public class EnergyMixResource implements Managed {
    
    private final EnergyMixHandler handler;
    
    EnergyMixResource(EnergyMixHandler handler) {
        this.handler = Preconditions.checkNotNull(handler);
    }
    
    @Override
    public void start() {
        handler.start();
    }
    
    @Override
    public void stop() {
        handler.stop();
    }
    
    @GET
    @Path("/ping")
    public String ping() {
        return "pong!";
    }
    
    @GET
    @Path("/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public EnergyMix getLatest() {
        return handler.getLatest();
    }
    
}
