package com.example.mobile.data.remote.api

import com.example.mobile.data.model.agendamento.AgendamentoRequest
import com.example.mobile.data.model.agendamento.AgendamentoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AgendamentoApi {
    @POST("agendamentos")
    fun criarAgendamento(@Body request: AgendamentoRequest): Call<AgendamentoResponse>
}