package nl.bertriksikken.theice;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public final class IceClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(IceClient.class);

    private static final String MARKETID_DUTCH_TTF_GAS_FUTURES = "5460494";
    private static final String PRODUCTID = "4331";
    private static final String HUBID = "7979";

    private final IIceRestApi restApi;

    IceClient(IIceRestApi restApi) {
        this.restApi = Preconditions.checkNotNull(restApi);
    }

    public static IceClient create(IceConfig config) {
        LOG.info("Creating new REST client for URL '{}' with timeout {}", config.getUrl(), config.getTimeout());
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(config.getTimeout())
                .readTimeout(config.getTimeout()).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create()).client(client).build();
        IIceRestApi restApi = retrofit.create(IIceRestApi.class);
        return new IceClient(restApi);
    }

    public IntradayChartData getIntradayChartData() throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("getIntradayChartDataAsJson", "");
        map.put("marketId", MARKETID_DUTCH_TTF_GAS_FUTURES);
        Response<String> response = restApi.getMarketData(map).execute();
        if (response.isSuccessful()) {
            IntradayChartData data = OBJECT_MAPPER.readValue(response.body(), IntradayChartData.class);
            return data;
        } else {
            LOG.warn("getMarketData failed, code {}, message {}", response.code(), response.message());
            return null;
        }
    }

    public List<Contract> getContracts() throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("getContractsAsJson", "");
        map.put("productId", PRODUCTID);
        map.put("hubId", HUBID);
        Response<String> response = restApi.getMarketData(map).execute();
        if (response.isSuccessful()) {
            List<Contract> data = OBJECT_MAPPER.readValue(response.body(), new TypeReference<List<Contract>>() {
            });
            return data;
        } else {
            LOG.warn("getMarketData failed, code {}, message {}", response.code(), response.message());
            return null;
        }
    }
}
