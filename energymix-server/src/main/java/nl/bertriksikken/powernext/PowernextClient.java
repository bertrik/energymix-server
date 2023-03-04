package nl.bertriksikken.powernext;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public final class PowernextClient {

    private static final Logger LOG = LoggerFactory.getLogger(PowernextClient.class);

    private final IPowernextApi restApi;

    PowernextClient(IPowernextApi restApi) {
        this.restApi = Objects.requireNonNull(restApi);
    }

    public static PowernextClient create(PowernextConfig config) {
        LOG.info("Creating new REST client for URL '{}' with timeout {}", config.getUrl(), config.getTimeout());
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(config.getTimeout())
                .readTimeout(config.getTimeout()).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create()).client(client).build();
        IPowernextApi restApi = retrofit.create(IPowernextApi.class);
        return new PowernextClient(restApi);
    }

    public FileResponse getCurrentPriceDocument() throws IOException {
        Response<String> response = restApi.downloadFile(IPowernextApi.NGP_CURRENT_PRICES).execute();
        if (response.isSuccessful()) {
            return FileResponse.create(response.body(), response.headers().get("Last-Modified"));
        } else {
            LOG.warn("getCurrentPrice failed, code {}, message {}", response.code(), response.message());
            return FileResponse.empty();
        }
    }
}
