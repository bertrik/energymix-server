package nl.bertriksikken.energymix.entsog;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.bertriksikken.energymix.entsoe.EntsoeClient;
import nl.bertriksikken.entsog.EntsogAggregatedData;
import nl.bertriksikken.entsog.EntsogRequest;
import nl.bertriksikken.entsog.IEntsogRestApi;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public final class EntsogClient {

    private static final Logger LOG = LoggerFactory.getLogger(EntsoeClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final IEntsogRestApi restApi;
    private final ObjectMapper mapper;

    EntsogClient(IEntsogRestApi restApi, ObjectMapper mapper) {
        this.restApi = Objects.requireNonNull(restApi);
        this.mapper = Objects.requireNonNull(mapper);
    }

    public static EntsogClient create(EntsogClientConfig config) {
        Duration timeout = config.getTimeout();
        LOG.info("Creating new REST client for URL '{}' with timeout {}", config.getUrl(), timeout);
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(timeout).readTimeout(timeout)
                .writeTimeout(timeout).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create()).client(client).build();
        IEntsogRestApi restApi = retrofit.create(IEntsogRestApi.class);
        return new EntsogClient(restApi, MAPPER.findAndRegisterModules());
    }

    String getRawDocument(Map<String, String> params) throws IOException {
        Response<String> response = restApi.getAggregatedData(params).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            LOG.warn("Got error: {}", response.errorBody().string());
            throw new EntsogClientException(response.errorBody().string());
        }
    }

    public EntsogAggregatedData getAggregatedData(EntsogRequest request) throws IOException {
        // get raw document
        String json = getRawDocument(request.getParams());

        // parse as response
        return mapper.readValue(json, EntsogAggregatedData.class);
    }

}
