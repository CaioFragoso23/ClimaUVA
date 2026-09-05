package com.example.climauva.service;
import com.example.climauva.model.OpenMeteoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface WeatherApiService {
    @GET("forecast")
    Call<OpenMeteoResponse> getCurrentWeather(
            @Query("latitude") double lat,
            @Query("longitude") double lon,
            @Query("current") String currentParams
    );
}
