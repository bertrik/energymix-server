package nl.bertriksikken.berthub;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * REST-like interface to berthub pre-processed production data.
 */
public interface IBertHubApi {

    @GET("/nlelec/harvested/{filename}")
    Call<String> downloadHarvested(@Path("filename") String fileName);

}
