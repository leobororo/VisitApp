package com.leandrobororo.visitapp.services;

import com.leandrobororo.visitapp.dto.MatchingVisitsReturn;
import com.leandrobororo.visitapp.model.Visit;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by 06524851625 on 13/04/2016.
 */
public interface APIBackendService {
        String BASE_URL = "http://10.0.2.2:8080/api/";

        @GET("visits")
        Call<List<Visit>> getVisits(@Query("idFacebook") String idFacebook, @Query("accessToken") String accessToken);

        @POST("visits")
        Call<ResponseBody> saveVisit(@Body Visit visit, @Query("idFacebook") String idFacebook, @Query("accessToken") String accessToken);

        @DELETE("visits/{id}")
        Call<ResponseBody> deletVisit(@Path("id") String id, @Query("idFacebook") String idFacebook, @Query("accessToken") String accessToken);

        @GET("matchingvisits")
        Call<MatchingVisitsReturn> getMatchingVisits(@Query("idFacebook") String idFacebook, @Query("accessToken") String accessToken, @Query("idsFacebookFriend") String idFacebookFriend, @Query("idPlace") String idPlace, @Query("date") String date, @Query("startHour") int startHour, @Query("endHour") int endHour);
}
