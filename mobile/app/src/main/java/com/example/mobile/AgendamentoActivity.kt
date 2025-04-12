package com.example.mobile

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Immutable

class AgendamentoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agendamento)
        var idCliente = intent.getStringExtra("ID")



    }
}

@Immutable
data class Servico(
    val name: String,
    val price: Double
)

// Create the list of Servico objects
val servicosList: List<Servico> = listOf(
    Servico(
        name = "Brasileiro",
        price = 130.00
    ),
    Servico(
        name = "Combo",
        price = 180.00
    ),
    Servico(
        name = "Design",
        price = 35.00
    ),
    Servico(
        name = "Fox",
        price = 190.00
    ),
    Servico(
        name = "Henna",
        price = 45.00
    ),
    Servico(
        name = "Hibrido",
        price = 130.00
    ),
    Servico(
        name = "Labio",
        price = 200.00
    ),
    Servico(
        name = "Mega",
        price = 160.00
    ),
    Servico(
        name = "Micropigmentacao",
        price = 250.00
    ),
    Servico(
        name = "Natural",
        price = 100.00
    ),
    Servico(
        name = "Shadow",
        price = 280.00
    ),
    Servico(
        name = "Russo",
        price = 150.00
    ),
    Servico(
        name = "Completo",
        price = 200.00
    ),
    Servico(
        name = "Kardashian",
        price = 150.00
    ),
    Servico(
        name = "Labio Natural",
        price = 250.00
    )
)
