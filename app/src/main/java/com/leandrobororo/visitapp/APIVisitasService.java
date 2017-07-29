package com.leandrobororo.visitapp;

import com.leandrobororo.visitapp.model.RetornoAPIVisitas;
import com.leandrobororo.visitapp.model.Visita;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;


/**
 * Created by 06524851625 on 13/04/2016.
 */
public interface APIVisitasService {
        String BASE_URL = "https://parseapi.back4app.com/classes/";
        String HEADER_APP_ID = "X-Parse-Application-Id: 32FhpZF70irk91ykDXOmAL6at1aqcBq1GppYDMuU";
        String HEADER_REST_KEY = "X-Parse-REST-API-Key: l1WTcbgboH6edRdUuu0ldW2H1yIdUGVg2F48Yykd";

        @Headers({HEADER_APP_ID, HEADER_REST_KEY})
        @GET("visita")
        Call<RetornoAPIVisitas> getVisitas();

        @Headers({HEADER_APP_ID, HEADER_REST_KEY})
        @POST("visita")
        Call<ResponseBody> salvarVisita(@Body Visita visita);
}
