package nl.bertriksikken.energymix.ned;

import nl.bertriksikken.ned.EActivity;
import nl.bertriksikken.ned.EClassification;
import nl.bertriksikken.ned.EEnergyType;
import nl.bertriksikken.ned.EGranularity;
import nl.bertriksikken.ned.EPoint;
import nl.bertriksikken.ned.INednlRestApi;
import nl.bertriksikken.ned.UtilizationJson;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class NedClient {

    private static final Logger LOG = LoggerFactory.getLogger(NedClient.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final String USER_AGENT = "github.com/bertrik/energymix-server";

    private final OkHttpClient httpClient;
    private final INednlRestApi restApi;
    private final NedConfig config;

    NedClient(OkHttpClient httpClient, INednlRestApi restApi, NedConfig config) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.restApi = Objects.requireNonNull(restApi);
        this.config = Objects.requireNonNull(config);
    }

    public static NedClient create(NedConfig config) {
        Duration timeout = config.getTimeout();
        LOG.info("Creating new REST client for URL '{}' with timeout {}", config.getUrl(), timeout);
        OkHttpClient httpClient = new OkHttpClient().newBuilder().connectTimeout(timeout).readTimeout(timeout)
                .writeTimeout(timeout)
                .addInterceptor(NedClient::addUserAgent)
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(JacksonConverterFactory.create()).client(httpClient).build();
        INednlRestApi restApi = retrofit.create(INednlRestApi.class);
        return new NedClient(httpClient, restApi, config);
    }

    public void shutdown() {
        httpClient.dispatcher().executorService().shutdown();
    }

    private static okhttp3.Response addUserAgent(Chain chain) throws IOException {
        Request userAgentRequest = chain.request().newBuilder().header("User-Agent", USER_AGENT).build();
        return chain.proceed(userAgentRequest);
    }

    public List<UtilizationJson> getUtilizations(Instant instant, EEnergyType energyType, EGranularity granularity) throws IOException {
        LocalDate today = LocalDate.ofInstant(instant, ZoneOffset.UTC);

        // note: parameter order is important
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("point", EPoint.NEDERLAND.getValue()); // 1
        params.put("type", energyType.getValue()); // 2
        params.put("granularity", granularity.getValue()); // 3
        params.put("granularitytimezone", EGranularity.ETimeZone.UTC.getValue()); // 4
        params.put("classification", EClassification.CURRENT.getValue()); // 5
        params.put("activity", EActivity.PROVIDING.getValue()); // 6
        params.put("validfrom[after]", today.format(DATE_FORMATTER)); // 7
        params.put("validfrom[strictly_before]", today.plusDays(1).format(DATE_FORMATTER)); // 8

        Response<List<UtilizationJson>> response = restApi.getUtilizations(config.getApiKey(), params).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            LOG.warn("Got error: {}", response.errorBody().string());
            throw new NedClientException(response.errorBody().string());
        }
    }
}
