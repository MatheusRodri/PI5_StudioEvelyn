package com.example.mobile.data.remote.provider

import com.example.mobile.data.remote.RetrofitClient
import com.example.mobile.data.remote.api.CadastroApi

object CadastroProvider{
    val cadastroApi:CadastroApi by lazy{
        RetrofitClient.retrofit.create(CadastroApi::class.java)
    }
}