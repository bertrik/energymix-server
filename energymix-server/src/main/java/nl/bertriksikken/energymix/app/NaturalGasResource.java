package nl.bertriksikken.energymix.app;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import es.moki.ratelimij.dropwizard.annotation.Rate;
import es.moki.ratelimij.dropwizard.annotation.RateLimited;
import es.moki.ratelimij.dropwizard.filter.KeyPart;
import io.dropwizard.jersey.caching.CacheControl;
import io.dropwizard.lifecycle.Managed;
import nl.bertriksikken.energymix.server.NaturalGasHandler;
import nl.bertriksikken.naturalgas.NeutralGasPrices;
import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice;

@Path("/naturalgas")
public final class NaturalGasResource implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(NaturalGasResource.class);

    private final NaturalGasHandler handler;

    NaturalGasResource(NaturalGasHandler handler) {
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
    @Path("/price")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(maxAge = 15, maxAgeUnit = TimeUnit.MINUTES)
    @RateLimited(keys = KeyPart.ANY, rates = { @Rate(duration = 1, timeUnit = TimeUnit.MINUTES, limit = 10) })
    public NaturalGasPrice getPrices() {
        // get data from handler
        NeutralGasPrices neutralGasPrices = handler.getGasPrices();

        // determine todays price (assume this is the first price in the list)
        NeutralGasDayPrice finalPrice = neutralGasPrices.findFinalPrice();
        if (finalPrice != null) {
            // build JSON response
            NaturalGasPrice naturalGasPrice = new NaturalGasPrice(finalPrice);
            neutralGasPrices.getTemporaryPrices().forEach(p -> naturalGasPrice.addDayAheadPrice(p));
            return naturalGasPrice;
        } else {
            LOG.warn("Could not determine a final gas price");
            return null;
        }
    }

    // JSON representation of natural gas price
    private static final class NaturalGasPrice {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;;

        @JsonProperty("current")
        private final NaturalGasDayPrice currentPrice;
        @JsonProperty("day-ahead")
        private final List<NaturalGasDayPrice> dayAheadPrices = new ArrayList<>();

        private NaturalGasPrice(NeutralGasDayPrice currentPrice) {
            this.currentPrice = new NaturalGasDayPrice(currentPrice);
        }

        private void addDayAheadPrice(NeutralGasDayPrice entry) {
            dayAheadPrices.add(new NaturalGasDayPrice(entry));
        }

        private static final class NaturalGasDayPrice {
            @JsonProperty("date")
            private String date;
            @JsonProperty("price")
            private double price;

            private NaturalGasDayPrice(NeutralGasDayPrice entry) {
                this.date = DATE_FORMATTER.format(entry.date);
                this.price = entry.indexValue;
            }

            @Override
            public String toString() {
                return String.format(Locale.ROOT, "{%s,%s,%s}", date, price);
            }
        }
    }

}
