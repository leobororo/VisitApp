package com.leandrobororo.visitapp;

import com.leandrobororo.visitapp.model.RetornoAPIVisitas;
import com.leandrobororo.visitapp.model.Visita;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;


/**
 * Created by 06524851625 on 13/04/2016.
 */
public interface APIBackendVisitasService {
        String BASE_URL = "http://10.0.2.2:8080/api/";

        @GET("values")
        Call<List<Visita>> getVisitas(@Query("idFacebook") String idFacebook);

        @POST("values")
        Call<ResponseBody> salvarVisita(@Body Visita visita);
}
