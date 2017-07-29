package com.leandrobororo.visitapp;

import com.leandrobororo.visitapp.model.RetornoCallWeather;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


/**
 * Created by 06524851625 on 13/04/2016.
 */
public interface APIWeatherService {
        String BASE_URL = "http://api.openweathermap.org/data/2.5/";

        @GET("forecast?lang=pt&units=metric&appid=9a757f11c430d12f1f7584f8138ca000")
        Call<RetornoCallWeather> obterPrevisao(@Query("lat") double latitude, @Query("lon") double longitude);
}
