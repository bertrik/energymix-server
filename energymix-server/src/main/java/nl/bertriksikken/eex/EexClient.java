package nl.bertriksikken.eex;

import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

public final class EexClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EexClient.class);

    private final OkHttpClient httpClient;
    private final IEexApi restApi;

    EexClient(OkHttpClient httpClient, IEexApi restApi) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.restApi = Objects.requireNonNull(restApi);
    }

    public static EexClient create(EexConfig config) {
        LOG.info("Creating new REST client for URL '{}' with timeout {}", config.getUrl(), config.getTimeout());

        // build the api through retrofit
        OkHttpClient httpClient = createHttpClient(config.getTimeout());
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create()).client(httpClient).build();
        IEexApi restApi = retrofit.create(IEexApi.class);
        return new EexClient(httpClient, restApi);
    }

    // create client with specified timeouts and specific trust store for EEX
    private static OkHttpClient createHttpClient(Duration timeout) {
        return new OkHttpClient().newBuilder().connectTimeout(timeout)
                .readTimeout(timeout)
                .build();
    }

    @Override
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    public FileResponse getCurrentPriceDocument() throws IOException {
        Response<String> response = restApi.downloadFile(CurrentPriceDocument.NGP_CURRENT_PRICES).execute();
        if (response.isSuccessful()) {
            return FileResponse.create(response.body(), response.headers().get("Last-Modified"));
        } else {
            LOG.warn("getCurrentPrice failed, code {}, message {}", response.code(), response.message());
            return FileResponse.empty();
        }
    }
}
