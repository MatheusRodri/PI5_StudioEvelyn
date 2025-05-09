package com.example.mobile.data.remote.provider

import com.example.mobile.data.remote.RetrofitClient
import com.example.mobile.data.remote.api.AgendamentoApi

object AgendamentoProvider {
    val agendamentoApi: AgendamentoApi by lazy {
        RetrofitClient.retrofit.create(AgendamentoApi::class.java)
    }
}