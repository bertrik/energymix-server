package nl.bertriksikken.entsog;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface IEntsogRestApi {

    @GET("/api/v2/aggregatedData.json")
    Call<String> getAggregatedData(@QueryMap Map<String, String> params);

}
