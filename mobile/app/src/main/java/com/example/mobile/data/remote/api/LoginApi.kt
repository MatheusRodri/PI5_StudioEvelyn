package com.example.mobile.data.remote.api

import com.example.mobile.data.model.login.LoginRequest
import com.example.mobile.data.model.login.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<List<LoginResponse>>
}
