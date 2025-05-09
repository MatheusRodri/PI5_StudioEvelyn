package com.example.mobile.data.remote.provider

import com.example.mobile.data.remote.RetrofitClient
import com.example.mobile.data.remote.api.AgendamentosApi

object AgendamentosProvider{
    val agendamentosApi:AgendamentosApi by lazy {
        RetrofitClient.retrofit.create(AgendamentosApi::class.java)
    }
}