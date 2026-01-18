package nl.bertriksikken.eex;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IEexApi {

    /**
     * Downloads a file from the EEX download center.
     */
    @GET("/Gas/NGP/{filename}")
    Call<String> downloadFile(@Path("filename") String filename);

}
