package com.example.mobile

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Immutable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Locale

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
    ServicoAtualizar(name = "Fox", price = 190.00),
    ServicoAtualizar(name = "Henna", price = 45.00),
    ServicoAtualizar(name = "Hibrido", price = 130.00),
    ServicoAtualizar(name = "Labio", price = 200.00),
    ServicoAtualizar(name = "Mega", price = 160.00),
    ServicoAtualizar(name = "Micropigmentacao", price = 250.00),
    ServicoAtualizar(name = "Natural", price = 100.00),
    ServicoAtualizar(name = "Shadow", price = 280.00),
    ServicoAtualizar(name = "Russo", price = 150.00),
    ServicoAtualizar(name = "Completo", price = 200.00),
    ServicoAtualizar(name = "Kardashian", price = 150.00),
    ServicoAtualizar(name = "Labio Natural", price = 250.00)
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

class Atualiza_Agendamento_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_atualiza_agendamento)

        val id_agendamento = intent.getIntExtra("ID_AGENDAMENTO", -1)
        val id_cliente = intent.getIntExtra("ID_CLIENTE", -1)
        val nome_agendamento = intent.getStringExtra("NOME_PROCEDIMENTO")
        val hora_agendamento = intent.getStringExtra("HORA_AGENDAMENTO")
        val data_agendamento = intent.getStringExtra("DATA_AGENDAMENTO")
        val valor_agendamento = intent.getStringExtra("VALOR_TOTAL")
        val forma_pagamento_agendamento = intent.getStringExtra("FORMA_PAGAMENTO_AGENDAMENTO")

        Log.d("Atualiza", "id agendamento: $id_agendamento\nid_cliente: $id_cliente")

        val edtData = findViewById<EditText>(R.id.dataAgendamentoEditar)
        val edtHora = findViewById<EditText>(R.id.horaAgendamentoEditar)
        val textoValor = findViewById<TextView>(R.id.textoValorDinamicoEditar)

        val chkPix = findViewById<RadioButton>(R.id.chkPixEditar)
        val chkCartao = findViewById<RadioButton>(R.id.chkCartaoEditar)

        val checkboxes = listOf(
            R.id.cbDesignEditar,
            R.id.cbBrasileiroEditar,
            R.id.cbComboEditar,
            R.id.cbFoxEditar,
            R.id.cbHennaEditar,
            R.id.cbHibridoEditar,
            R.id.cbLabioEditar,
            R.id.cbNaturalEditar,
            R.id.cbMegaEditar
        ).map { findViewById<CheckBox>(it) }

        edtData.setText(data_agendamento?.formatarDataISOParaBR())
        edtHora.setText(hora_agendamento)

        when (forma_pagamento_agendamento) {
            "Pix" -> chkPix.isChecked = true
            "Cartão" -> chkCartao.isChecked = true
        }

        nome_agendamento?.split(",")?.map { it.trim() }?.forEach { procedimento ->
            checkboxes.find { it.text.contains(procedimento) }?.isChecked = true
        }

        // Listener de atualização de valor dinâmico
        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                val procedimentosSelecionados = obterNomesProcedimentosSelecionadosDoXml()
                val precoTotal = calcularPrecoTotal(procedimentosSelecionados)
                textoValor.text = "Total: R$ %.2f".format(precoTotal)
            }
        }

        // Atualiza o valor inicialmente
        val precoInicial = calcularPrecoTotal(obterNomesProcedimentosSelecionadosDoXml())
        textoValor.text = "Total: R$ %.2f".format(precoInicial)

        val btnEditar = findViewById<Button>(R.id.btnAgendarEditar)
        btnEditar.setOnClickListener {
            val procedimentosSelecionados = obterNomesProcedimentosSelecionadosDoXml()
            val precoTotal = calcularPrecoTotal(procedimentosSelecionados)
            val formaPagamento = when {
                chkPix.isChecked -> "Pix"
                chkCartao.isChecked -> "Cartão"
                else -> ""
            }

            val request = NetworkConfigAtualizaAgendamento.AgendamentoAtualizarRequest(
                ID = id_agendamento,
                DATA = edtData.text.toString().formatarDataBRparaMySQL(),
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
        val ids = listOf(
            R.id.cbDesignEditar to "Design",
            R.id.cbBrasileiroEditar to "Brasileiro",
            R.id.cbComboEditar to "Combo",
            R.id.cbFoxEditar to "Fox",
            R.id.cbHennaEditar to "Henna",
            R.id.cbHibridoEditar to "Hibrido",
            R.id.cbLabioEditar to "Labio",
            R.id.cbNaturalEditar to "Natural",
            R.id.cbMegaEditar to "Mega"
        )
        ids.forEach { (id, name) ->
            val checkBox = findViewById<CheckBox>(id)
            if (checkBox.isChecked) nomesSelecionados.add(name)
        }
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
            .enqueue(object :
                Callback<NetworkConfigAtualizaAgendamento.AgendamentoAtualizarResponse> {
                override fun onResponse(
                    call: Call<NetworkConfigAtualizaAgendamento.AgendamentoAtualizarResponse>,
                    response: Response<NetworkConfigAtualizaAgendamento.AgendamentoAtualizarResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@Atualiza_Agendamento_Activity, "Agendamento atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = response.errorBody()?.string()
                        Log.e("API_ERROR", "Erro: $error")
                        Toast.makeText(this@Atualiza_Agendamento_Activity, "Erro ao atualizar: $error", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(
                    call: Call<NetworkConfigAtualizaAgendamento.AgendamentoAtualizarResponse>,
                    t: Throwable
                ) {
                    Log.e("API_FAILURE", "Falha: ${t.message}", t)
                    Toast.makeText(this@Atualiza_Agendamento_Activity, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
