package nl.bertriksikken.eex;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(config.getTimeout())
                .readTimeout(config.getTimeout()).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create()).client(client).build();
        IEexApi restApi = retrofit.create(IEexApi.class);
        return new EexClient(client, restApi);
    }

    @Override
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    public FileResponse getCurrentPriceDocument() throws IOException {
        Response<String> response = restApi.downloadFile(IEexApi.NGP_CURRENT_PRICES).execute();
        if (response.isSuccessful()) {
            return FileResponse.create(response.body(), response.headers().get("Last-Modified"));
        } else {
            LOG.warn("getCurrentPrice failed, code {}, message {}", response.code(), response.message());
            return FileResponse.empty();
        }
    }
}
