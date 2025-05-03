package com.example.mobile

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Immutable
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.*





class AtualizaAgendamentoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_atualiza_agendamento_old)

        val id_agendamento = intent.getIntExtra("ID_AGENDAMENTO", -1)
        val id_cliente = intent.getIntExtra("ID_CLIENTE", -1)
        val nome_agendamento = intent.getStringExtra("NOME_PROCEDIMENTO")
        val hora_agendamento = intent.getStringExtra("HORA_AGENDAMENTO")
        val data_agendamento = intent.getStringExtra("DATA_AGENDAMENTO")
        val forma_pagamento_agendamento = intent.getStringExtra("FORMA_PAGAMENTO_AGENDAMENTO")

        Log.d("Atualiza", "id agendamento: $id_agendamento\nid_cliente: $id_cliente")

        val edtData = findViewById<EditText>(R.id.editData)
        val edtHora = findViewById<EditText>(R.id.editHoraAtualiza)

        val chkDinheiro = findViewById<RadioButton>(R.id.chkDinheiro)
        val chkPix = findViewById<RadioButton>(R.id.chkPix)
        val chkCartao = findViewById<RadioButton>(R.id.chkCartao)

        val chkDesign = findViewById<CheckBox>(R.id.chkDesign)
        val chkBrasileiro = findViewById<CheckBox>(R.id.chkBrasileiro)
        val chkCombo = findViewById<CheckBox>(R.id.chkCombo)

        edtData.setText(data_agendamento?.formatarDataISOParaBR())
        edtHora.setText(hora_agendamento)

        when (forma_pagamento_agendamento) {
            "Dinheiro" -> chkDinheiro.isChecked = true
            "Pix" -> chkPix.isChecked = true
            "Cartão" -> chkCartao.isChecked = true
        }

        nome_agendamento?.split(",")?.map { it.trim() }?.forEach { procedimento ->
            when (procedimento) {
                "Design" -> chkDesign.isChecked = true
                "Brasileiro" -> chkBrasileiro.isChecked = true
                "Combo" -> chkCombo.isChecked = true
            }
        }

        val btnConfirmar = findViewById<Button>(R.id.idBtnAgendarAtualizar)
        btnConfirmar.setOnClickListener {
            val procedimentosSelecionados = obterNomesProcedimentosSelecionadosDoXml()
            val precoTotal = calcularPrecoTotal(procedimentosSelecionados)
            val formaPagamento = when {
                chkDinheiro.isChecked -> "Dinheiro"
                chkPix.isChecked -> "Pix"
                chkCartao.isChecked -> "Cartão"
                else -> ""
            }

            val request = NetworkConfigAtualizaAgendamento.AgendamentoAtualizarRequest(
                ID = id_agendamento,
                DATA = edtData.text.toString().formatarDataBRparaMySQL(), // ✅ Aqui usamos a conversão correta
                HORA = edtHora.text.toString(),
                PROCEDIMENTO = procedimentosSelecionados.joinToString(", "),
                VALOR = precoTotal,
                TP_PAGAMENTO = formaPagamento,
                ID_CLIENT = id_cliente
            )

            atualizaAgendamentoApi(request)
        }
    }

    private fun obterNomesProcedimentosSelecionadosDoXml(): List<String> {
        val nomesSelecionados = mutableListOf<String>()
        val chkDesign = findViewById<CheckBox>(R.id.chkDesign)
        val chkBrasileiro = findViewById<CheckBox>(R.id.chkBrasileiro)
        val chkCombo = findViewById<CheckBox>(R.id.chkCombo)
        if (chkDesign.isChecked) nomesSelecionados.add("Design")
        if (chkBrasileiro.isChecked) nomesSelecionados.add("Brasileiro")
        if (chkCombo.isChecked) nomesSelecionados.add("Combo")
        return nomesSelecionados
    }

    private fun calcularPrecoTotal(nomesSelecionados: List<String>): Double {
        var precoTotal = 0.0
        nomesSelecionados.forEach { nome ->
            val servico = servicosListAtualizar.find { it.name.equals(nome, ignoreCase = true) }
            precoTotal += servico?.price ?: 0.0
        }
        return precoTotal
    }

    private fun atualizaAgendamentoApi(request: NetworkConfigAtualizaAgendamento.AgendamentoAtualizarRequest) {
        NetworkConfigAtualizaAgendamento.apiService.atualizarAgendamento(request)
            .enqueue(object : Callback<NetworkConfigAtualizaAgendamento.AgendamentoAtualizarResponse> {
                override fun onResponse(
                    call: Call<NetworkConfigAtualizaAgendamento.AgendamentoAtualizarResponse>,
                    response: Response<NetworkConfigAtualizaAgendamento.AgendamentoAtualizarResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@AtualizaAgendamentoActivity, "Agendamento atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = response.errorBody()?.string()
                        Log.e("API_ERROR", "Erro: $error")
                        Toast.makeText(this@AtualizaAgendamentoActivity, "Erro ao atualizar: $error", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(
                    call: Call<NetworkConfigAtualizaAgendamento.AgendamentoAtualizarResponse>,
                    t: Throwable
                ) {
                    Log.e("API_FAILURE", "Falha: ${t.message}", t)
                    Toast.makeText(this@AtualizaAgendamentoActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
