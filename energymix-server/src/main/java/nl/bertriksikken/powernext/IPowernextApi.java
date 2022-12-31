package nl.bertriksikken.powernext;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IPowernextApi {

    public static final String NGP_CURRENT_PRICES = "TTF_NGP_15_Mins.csv";

    /**
     * Downloads a file from the powernext download center.
     */
    @GET("/Gas/NGP/{filename}")
    Call<String> downloadFile(@Path("filename") String filename);

}
