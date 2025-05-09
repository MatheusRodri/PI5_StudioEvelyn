package com.example.mobile.data.remote.provider


import com.example.mobile.data.remote.RetrofitClient
import com.example.mobile.data.remote.api.LoginApi

object LoginProvider {
    val loginApi: LoginApi by lazy {
        RetrofitClient.retrofit.create(LoginApi::class.java)
    }
}