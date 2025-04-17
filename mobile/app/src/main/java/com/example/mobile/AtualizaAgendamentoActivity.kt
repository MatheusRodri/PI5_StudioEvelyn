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

// Função para formatar de ISO para BR (exibição)
fun String.formatarDataISOParaBR(): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = parser.parse(this)
        formatter.format(date!!)
    } catch (e: Exception) {
        this
    }
}

fun String.formatarDataBRparaMySQL(): String {
    return try {
        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(this)
        formatter.format(date!!)
    } catch (e: Exception) {
        this
    }
}


@Immutable
data class ServicoAtualizar(
    val name: String,
    val price: Double
)

val servicosListAtualizar: List<ServicoAtualizar> = listOf(
    ServicoAtualizar(name = "Brasileiro", price = 130.00),
    ServicoAtualizar(name = "Combo", price = 180.00),
    ServicoAtualizar(name = "Design", price = 35.00),
)

object NetworkConfigAtualizaAgendamento {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    val apiService: ApiServiceAtualizaAgendamento by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceAtualizaAgendamento::class.java)
    }

    interface ApiServiceAtualizaAgendamento {
        @POST("agendamentos/atualiza")
        fun atualizarAgendamento(@Body request: AgendamentoAtualizarRequest): Call<AgendamentoAtualizarResponse>
    }

    data class AgendamentoAtualizarRequest(
        val ID: Int,
        val DATA: String,
        val HORA: String,
        val PROCEDIMENTO: String,
        val VALOR: Double,
        val TP_PAGAMENTO: String,
        val ID_CLIENT: Int
    )

    data class AgendamentoAtualizarResponse(
        val message: String
    )
}

class AtualizaAgendamentoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_atualiza_agendamento)

        val id_agendamento = intent.getIntExtra("ID_AGENDAMENTO", -1)
        val id_cliente = intent.getIntExtra("ID_CLIENTE", -1)
        val nome_agendamento = intent.getStringExtra("NOME_PROCEDIMENTO")
        val hora_agendamento = intent.getStringExtra("HORA_AGENDAMENTO")
        val data_agendamento = intent.getStringExtra("DATA_AGENDAMENTO")
        val forma_pagamento_agendamento = intent.getStringExtra("FORMA_PAGAMENTO_AGENDAMENTO")

        Log.d("Atualiza", "id agendamento: $id_agendamento\nid_cliente: $id_cliente")

        val edtData = findViewById<EditText>(R.id.editDataAtualiza)
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
