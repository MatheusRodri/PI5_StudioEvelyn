package com.example.mobile.data.remote.api

import com.example.mobile.data.model.agendamento.cria.AgendamentoRequest
import com.example.mobile.data.model.agendamento.cria.AgendamentoResponse
import com.example.mobile.data.model.agendamento.atualiza.AgendamentoAtualizaRequest
import com.example.mobile.data.model.agendamento.atualiza.AgendamentoAtualizaResponse
import com.example.mobile.data.model.agendamento.excluir.AgendamentoExcluirRequest
import com.example.mobile.data.model.agendamento.excluir.AgendamentoExcluirResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AgendamentoApi {
    @POST("agendamentos")
    fun criarAgendamento(@Body request: AgendamentoRequest): Call<AgendamentoResponse>

    @POST("agendamentos/atualiza")
    fun atualizarAgendamento(@Body request: AgendamentoAtualizaRequest): Call<AgendamentoAtualizaResponse>

    @POST("agendamentos/excluir")
    fun excluirAgendamento(@Body requestBody: AgendamentoExcluirRequest): Call<AgendamentoExcluirResponse>
}
