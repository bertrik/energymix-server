package nl.bertriksikken.theice;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface IIceRestApi {

    @GET("/marketdata/DelayedMarkets.shtml")
    Call<String> getMarketData(@QueryMap Map<String, String> queryMap);

}
