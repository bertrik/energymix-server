package nl.bertriksikken.energymix.entsoe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Preconditions;

import nl.bertriksikken.entsoe.EntsoeRequest;
import nl.bertriksikken.entsoe.EntsoeResponse;
import nl.bertriksikken.entsoe.IEntsoeRestApi;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public final class EntsoeFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(EntsoeFetcher.class);

    private final IEntsoeRestApi restApi;
    private final EntsoeFetcherConfig config;
    private final XmlMapper mapper;

    EntsoeFetcher(IEntsoeRestApi restApi, EntsoeFetcherConfig config, XmlMapper mapper) {
        this.restApi = Preconditions.checkNotNull(restApi);
        this.config = Preconditions.checkNotNull(config);
        this.mapper = Preconditions.checkNotNull(mapper);
    }

    public static EntsoeFetcher create(EntsoeFetcherConfig config, XmlMapper mapper) {
        LOG.info("Creating new REST client for URL '{}' with timeout {}", config.getUrl(), config.getTimeout());
        OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(config.getTimeout()).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create()).client(client).build();
        IEntsoeRestApi restApi = retrofit.create(IEntsoeRestApi.class);
        return new EntsoeFetcher(restApi, config, mapper);
    }

    String getRawDocument(Map<String, String> requestParams) throws IOException {
        Map<String, String> params = new HashMap<>(requestParams);
        params.put("securityToken", config.getApiKey());

        Response<String> response = restApi.getDocument(params).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            LOG.warn("Got error: {}", response.errorBody().string());
            throw new EntsoeFetcherException(response.errorBody().string());
        }
    }

    public EntsoeResponse getDocument(EntsoeRequest request) throws IOException {
        // get raw document
        String xml = getRawDocument(request.getParams());
        
        // parse as response
        return mapper.readValue(xml, EntsoeResponse.class);
    }

}
