package com.brugia.eatwithme.placeapi

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceDetailService {

    @GET("details/json")
    fun PlaceDetail(
            @Query("key") apiKey: String,
            @Query("place_id") placeID: String
    ): Call<PlaceDetailResponse>

    companion object {
        private const val ROOT_URL = "https://maps.googleapis.com/maps/api/place/"

        fun create(): PlaceDetailService {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            val converterFactory = GsonConverterFactory.create()
            val retrofit = Retrofit.Builder()
                    .baseUrl(ROOT_URL)
                    .client(okHttpClient)
                    .addConverterFactory(converterFactory)
                    .build()
            return retrofit.create(PlaceDetailService::class.java)
        }
    }
}