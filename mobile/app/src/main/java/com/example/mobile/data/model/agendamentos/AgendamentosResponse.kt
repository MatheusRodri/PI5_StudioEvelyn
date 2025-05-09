package com.example.mobile.data.model.agendamentos

data class AgendamentosResponse(
    val ID: Int,
    val NOME: String?,
    val EMAIL: String?,
    val CPF: String?,
    val DATA: String?,
    val HORA: String?,
    val VALOR: Float,
    val PROCEDIMENTO: String?,
    val TP_PAGAMENTO: String?,
    val ID_AGENDAMENTO: Int,
    val ID_CLIENTE: Int
)