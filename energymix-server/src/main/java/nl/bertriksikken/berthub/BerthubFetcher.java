package nl.bertriksikken.berthub;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Fetches and processes a CSV from https://berthub.eu/nlelec/harvested/
 */
public final class BerthubFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(BerthubFetcher.class);

    private final IBertHubApi restApi;
    private final AtomicBoolean healthy = new AtomicBoolean(true);

    BerthubFetcher(IBertHubApi restApi) {
        this.restApi = Preconditions.checkNotNull(restApi);
    }

    // creates in instance of the fetcher, with the specified configuration
    public static BerthubFetcher create(BerthubFetcherConfig config) {
        LOG.info("Creating new REST client for URL '{}' with timeout {}", config.getUrl(), config.getTimeout());
        OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(config.getTimeout()).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create()).client(client).build();
        IBertHubApi restApi = retrofit.create(IBertHubApi.class);
        return new BerthubFetcher(restApi);
    }

    public String download(Instant now) throws IOException {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(now, ZoneOffset.UTC);
        String fileName = String.format(Locale.ROOT, "nlprod-%4d%02d%02d.csv", zonedDateTime.getYear(),
                zonedDateTime.getMonth().getValue(), zonedDateTime.getDayOfMonth());
        Response<String> response = restApi.downloadHarvested(fileName).execute();
        if (response.isSuccessful()) {
            healthy.set(true);
            return response.body();
        } else {
            healthy.set(false);
            LOG.warn("Fetching file {} failed!", fileName);
            return "";
        }
    }
    
    public boolean isHealthy() {
        return healthy.get();
    }

}
