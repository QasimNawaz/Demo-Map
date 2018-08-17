package com.example.alisons.demo_application.routes;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface RetrofitMaps {

    /*
     * Retrofit get annotation with our URL
     * And our method that will return us details of student.
     */
//    @GET("api/directions/json?key=AIzaSyAoP-lWkshvTaSQgUMPKb3nE-xcxmf5Z1s")
    @GET("api/directions/json?key=AIzaSyD0YTPfwgZl_dbNckhoSW66XorB7pSl4tw")
    Call<Example> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);
}
