package nl.bertriksikken.energymix.entsoe;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import nl.bertriksikken.entsoe.EntsoeRequest;
import nl.bertriksikken.entsoe.EntsoeResponse;
import nl.bertriksikken.entsoe.IEntsoeRestApi;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * See
 * https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html
 */
public final class EntsoeClient {

    private static final Logger LOG = LoggerFactory.getLogger(EntsoeClient.class);
    
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    private final IEntsoeRestApi restApi;
    private final EntsoeConfig config;
    private final XmlMapper mapper;

    EntsoeClient(IEntsoeRestApi restApi, EntsoeConfig config, XmlMapper mapper) {
        this.restApi = Objects.requireNonNull(restApi);
        this.config = Objects.requireNonNull(config);
        this.mapper = Objects.requireNonNull(mapper);
    }

    public static EntsoeClient create(EntsoeConfig config) {
        Duration timeout = config.getTimeout();
        LOG.info("Creating new REST client for URL '{}' with timeout {}", config.getUrl(), timeout);
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(timeout).readTimeout(timeout)
                .writeTimeout(timeout).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create()).client(client).build();
        IEntsoeRestApi restApi = retrofit.create(IEntsoeRestApi.class);
        return new EntsoeClient(restApi, config, XML_MAPPER);
    }

    String getRawDocument(Map<String, String> requestParams) throws IOException {
        Map<String, String> params = new HashMap<>(requestParams);
        params.put("securityToken", config.getApiKey());

        Response<String> response = restApi.getDocument(params).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            LOG.warn("Got error: {}", response.errorBody().string());
            throw new EntsoeClientException(response.errorBody().string());
        }
    }

    public EntsoeResponse getDocument(EntsoeRequest request) throws IOException {
        // get raw document
        String xml = getRawDocument(request.getParams());

        // parse as response
        return mapper.readValue(xml, EntsoeResponse.class);
    }

}
