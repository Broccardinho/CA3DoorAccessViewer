package com.example.dooraccess.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // JSONPlaceholder is a FREE public API for testing
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    //LOGGING INTERCEPTOR
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // Logs full request/response
    }

    // Create HTTP client with logger
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    //RETROFIT INSTANCE SETUP
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Create API service
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}