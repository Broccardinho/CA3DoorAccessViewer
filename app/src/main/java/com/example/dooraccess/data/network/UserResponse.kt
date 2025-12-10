package com.example.dooraccess.data.network

//RETROFIT DATA MODEL
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String = ""
)