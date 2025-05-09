package com.example.mobile.data.model.agendamento

data class AgendamentoRequest(
    val DATA: String,
    val HORA: String,
    val PROCEDIMENTO: String,
    val VALOR: Double,
    val TP_PAGAMENTO: String,
    val ID_CLIENT: Int
)