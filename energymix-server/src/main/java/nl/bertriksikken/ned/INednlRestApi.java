package nl.bertriksikken.ned;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

import java.util.List;
import java.util.Map;

public interface INednlRestApi {

    @GET("/v1/utilizations")
    @Headers({"Accept: application/json"})
    Call<List<UtilizationJson>> getUtilizations(@Header("X-AUTH-TOKEN") String apiKey, @QueryMap Map<String, Object> parameters);

}
