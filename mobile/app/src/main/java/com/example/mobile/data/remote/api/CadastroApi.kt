package com.example.mobile.data.remote.api


import com.example.mobile.data.model.cadastro.CadastroRequest
import com.example.mobile.data.model.cadastro.CadastroResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CadastroApi {
    @POST("cliente")
    fun cadastro(@Body request: CadastroRequest): Call<CadastroResponse>
}


