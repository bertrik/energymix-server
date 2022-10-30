package nl.bertriksikken.energymix.app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    @RateLimited(keys = KeyPart.ANY, rates = { @Rate(duration = 1, timeUnit = TimeUnit.MINUTES, limit = 10) })
    public NaturalGasPrice getPrices() {
        // get data from handler
        NeutralGasPrices neutralGasPrice = handler.getGasPrices();

        LocalDate today = LocalDate.now(NeutralGasPrices.NGP_TIME_ZONE);
        NeutralGasDayPrice current = neutralGasPrice.findDayPrice(today);

        // build JSON response
        NaturalGasPrice naturalGasPrice = new NaturalGasPrice(current);
        for (NeutralGasDayPrice dayPrice : neutralGasPrice.getDayPrices()) {
            if (dayPrice.indexVolume > 0) {
                naturalGasPrice.addDayPrice(dayPrice);
            }
        }
        return naturalGasPrice;
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

        private void addDayPrice(NeutralGasDayPrice entry) {
            dayAheadPrices.add(new NaturalGasDayPrice(entry));
        }

        private static final class NaturalGasDayPrice {
            @JsonProperty("date")
            private String date;
            @JsonProperty("price")
            private double price;
            @JsonProperty("status")
            private String status;

            private NaturalGasDayPrice(NeutralGasDayPrice entry) {
                this.date = DATE_FORMATTER.format(entry.date);
                this.price = entry.indexValue;
                this.status = entry.status.name();
            }

            @Override
            public String toString() {
                return String.format(Locale.ROOT, "{%s,%s,%s}", date, price, status);
            }
        }
    }

}
