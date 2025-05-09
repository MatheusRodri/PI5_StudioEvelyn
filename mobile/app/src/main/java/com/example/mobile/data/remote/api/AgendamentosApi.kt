package com.example.mobile.data.remote.api


import com.example.mobile.data.model.agendamentos.AgendamentosRequest
import com.example.mobile.data.model.agendamentos.AgendamentosResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AgendamentosApi{
    @POST("/agendamentos/cliente")
    suspend fun getagendamentos(@Body request: AgendamentosRequest): List<AgendamentosResponse>

}

