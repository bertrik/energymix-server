package nl.bertriksikken.entsoe;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface IEntsoeRestApi {

    @GET("/api")
    Call<String> getDocument(@QueryMap Map<String, String> params);

}
