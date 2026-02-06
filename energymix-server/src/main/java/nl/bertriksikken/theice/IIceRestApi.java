package nl.bertriksikken.theice;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface IIceRestApi {

    @GET("/marketdata/api/productguide/charting/contract-data")
    Call<String> getMarketData(@QueryMap Map<String, String> queryMap);

}
