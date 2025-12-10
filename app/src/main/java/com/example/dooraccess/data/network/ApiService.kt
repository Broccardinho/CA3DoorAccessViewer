package com.example.dooraccess.data.network

import retrofit2.Response
import retrofit2.http.GET

//RETROFIT API INTERFACE
interface ApiService {
    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>
}