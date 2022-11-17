package nl.bertriksikken.energymix.app;

import java.time.Instant;
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
import nl.bertriksikken.naturalgas.FutureGasPrices;
import nl.bertriksikken.naturalgas.FutureGasPrices.FutureGasPrice;
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
        NaturalGasPrice naturalGasPrice = new NaturalGasPrice();

        // build day-ahead part
        NeutralGasPrices neutralGasPrices = handler.getNeutralGasPrices();
        NeutralGasDayPrice finalPrice = neutralGasPrices.findFinalPrice();
        if (finalPrice != null) {
            naturalGasPrice.addDayAheadPrice(finalPrice);
            neutralGasPrices.getTemporaryPrices().forEach(p -> naturalGasPrice.addDayAheadPrice(p));
        } else {
            LOG.warn("Could not determine a final gas price");
        }

        // build month-ahead part
        FutureGasPrices futureGasPrices = handler.getFutureGasPrices();
        futureGasPrices.getPrices().stream().limit(3).forEach(p -> naturalGasPrice.addFutureGasPrice(p));

        return naturalGasPrice;
    }

    // JSON representation of natural gas price
    private static final class NaturalGasPrice {

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

        @JsonProperty("day-ahead")
        private final List<PriceAtDate> dayAheadPrices = new ArrayList<>();

        @JsonProperty("month-ahead")
        private final List<PriceAtDate> monthAheadPrices = new ArrayList<>();

        private void addDayAheadPrice(NeutralGasDayPrice price) {
            dayAheadPrices.add(new PriceAtDate(price.indexValue, DATE_FORMAT.format(price.date), price.timestamp));
        }

        private void addFutureGasPrice(FutureGasPrice price) {
            monthAheadPrices.add(new PriceAtDate(price.price, price.period, price.time));
        }

        private static final class PriceAtDate {

            private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;

            @JsonProperty("price")
            private final double price;
            @JsonProperty("date")
            private final String date;
            @JsonProperty("timestamp")
            private final String timestamp;

            private PriceAtDate(double price, String date, Instant timestamp) {
                this.price = price;
                this.date = date;
                this.timestamp = TIMESTAMP_FORMAT.format(timestamp);
            }

            @Override
            public String toString() {
                return String.format(Locale.ROOT, "{%.3f @ %s}", price, date);
            }
        }
    }

}
