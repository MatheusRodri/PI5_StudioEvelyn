package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Immutable
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Immutable
data class Servico(
    val name: String,
    val price: Double
)

val servicosList: List<Servico> = listOf(
    Servico(name = "Brasileiro", price = 130.00),
    Servico(name = "Combo", price = 180.00),
    Servico(name = "Design", price = 35.00),
    Servico(name = "Fox", price = 190.00),
    Servico(name = "Henna", price = 45.00),
    Servico(name = "Hibrido", price = 130.00),
    Servico(name = "Labio", price = 200.00),
    Servico(name = "Mega", price = 160.00),
    Servico(name = "Micropigmentacao", price = 250.00),
    Servico(name = "Natural", price = 100.00),
    Servico(name = "Shadow", price = 280.00),
    Servico(name = "Russo", price = 150.00),
    Servico(name = "Completo", price = 200.00),
    Servico(name = "Kardashian", price = 150.00),
    Servico(name = "Labio Natural", price = 250.00)
)

object NetworkConfigAgendamento {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    val apiService: ApiServiceAgendamentoCli by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceAgendamentoCli::class.java)
    }
}

interface ApiServiceAgendamentoCli {
    @POST("agendamentos")
    fun criarAgendamento(@Body request: AgendamentoRequest): Call<AgendamentoCliResponse>
}

data class AgendamentoRequest(
    val DATA: String,
    val HORA: String,
    val PROCEDIMENTO: String,
    val VALOR: Double,
    val TP_PAGAMENTO: String,
    val ID_CLIENT: Int
)

data class AgendamentoCliResponse(val message: String)

class AgendamentoActivity : AppCompatActivity() {

    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var radioGroupPagamento: RadioGroup
    private lateinit var btnAgendar: Button
    private lateinit var textoValor: TextView

    private var idCliente: Int? = null

    private val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val inputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val apiTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agendamento)

        editTextDate = findViewById(R.id.dataAgendamento)
        editTextTime = findViewById(R.id.horaAgendamento)
        radioGroupPagamento = findViewById(R.id.radioGroup)
        btnAgendar = findViewById(R.id.btnAgendar)
        textoValor = findViewById(R.id.textoValorDinamico)

        val idClienteStr: String? = intent.getStringExtra("ID")
        idCliente = idClienteStr?.toIntOrNull()
        if (idCliente == null) {
            mostrarAlertaSimples("Erro: Identificação do cliente inválida.")
            return
        }

        // Atualiza o valor total sempre que qualquer checkbox muda
        servicosList.forEach { servico ->
            val checkBoxId = resources.getIdentifier(
                "cb" + servico.name.replace(" ", ""),
                "id",
                packageName
            )
            val checkBox = findViewById<CheckBox>(checkBoxId)
            checkBox?.setOnCheckedChangeListener { _, _ -> atualizarValorTotal() }
        }

        atualizarValorTotal()

        btnAgendar.setOnClickListener {
            val dataStr = editTextDate.text.toString().trim()
            val horaStr = editTextTime.text.toString().trim()
            val nomesProcedimentosSelecionados = obterNomesProcedimentosSelecionadosDoXml()
            val precoTotal = calcularPrecoTotal(nomesProcedimentosSelecionados)

            val selectedPaymentId = radioGroupPagamento.checkedRadioButtonId
            val formaPagamento = if (selectedPaymentId != -1) {
                findViewById<RadioButton>(selectedPaymentId).text.toString()
            } else {
                mostrarAlertaSimples("Por favor, selecione uma forma de pagamento.")
                return@setOnClickListener
            }

            if (dataStr.isEmpty() || horaStr.isEmpty() || nomesProcedimentosSelecionados.isEmpty()) {
                mostrarAlertaSimples("Preencha todos os campos corretamente.")
                return@setOnClickListener
            }

            val dataFormatadaApi: String
            val horaFormatadaApi: String
            val procedimentosStr = nomesProcedimentosSelecionados.joinToString(",")

            try {
                val utilDate = inputDateFormat.parse(dataStr)
                val utilTime = inputTimeFormat.parse(horaStr)
                if (utilDate == null || utilTime == null) throw ParseException("Formato inválido", 0)
                dataFormatadaApi = apiDateFormat.format(utilDate)
                horaFormatadaApi = apiTimeFormat.format(utilTime)
            } catch (e: ParseException) {
                mostrarAlertaSimples("Formato inválido. Use dd/MM/yyyy e HH:mm.")
                return@setOnClickListener
            }

            val request = AgendamentoRequest(
                DATA = dataFormatadaApi,
                HORA = horaFormatadaApi,
                PROCEDIMENTO = procedimentosStr,
                VALOR = precoTotal,
                TP_PAGAMENTO = formaPagamento,
                ID_CLIENT = idCliente!!
            )

            criarAgendamentoApi(request)
        }
    }

    private fun atualizarValorTotal() {
        val selecionados = obterNomesProcedimentosSelecionadosDoXml()
        val precoTotal = calcularPrecoTotal(selecionados)
        textoValor.text = "Total: R$ %.2f".format(precoTotal)
    }

    private fun obterNomesProcedimentosSelecionadosDoXml(): List<String> {
        val nomesSelecionados = mutableListOf<String>()
        servicosList.forEach { servico ->
            val checkBoxId = resources.getIdentifier("cb" + servico.name.replace(" ", ""), "id", packageName)
            val checkBox = findViewById<CheckBox>(checkBoxId)
            if (checkBox?.isChecked == true) {
                nomesSelecionados.add(servico.name)
            }
        }
        return nomesSelecionados
    }

    private fun calcularPrecoTotal(nomesSelecionados: List<String>): Double {
        return nomesSelecionados.sumOf { nome ->
            servicosList.find { it.name.equals(nome, ignoreCase = true) }?.price ?: 0.0
        }
    }

    private fun mostrarAlertaSimples(mensagem: String) {
        AlertDialog.Builder(this)
            .setTitle("Aviso")
            .setMessage(mensagem)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun mostrarAlertaSucessoAgendamento(mensagem: String) {
        AlertDialog.Builder(this)
            .setTitle("Sucesso")
            .setMessage(mensagem)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun criarAgendamentoApi(request: AgendamentoRequest) {
        NetworkConfigAgendamento.apiService.criarAgendamento(request)
            .enqueue(object : Callback<AgendamentoCliResponse> {
                override fun onResponse(
                    call: Call<AgendamentoCliResponse>,
                    response: Response<AgendamentoCliResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        mostrarAlertaSucessoAgendamento(response.body()!!.message)
                        var intensao = Intent(this@AgendamentoActivity, AgendamentosActivity::class.java)

                        startActivity(intensao)
                        finish()
                    } else {
                        val errorDetails = try {
                            response.errorBody()?.string() ?: "Erro desconhecido."
                        } catch (e: Exception) {
                            "Erro ao ler erroBody: ${e.message}"
                        }
                        mostrarAlertaSimples("Erro ${response.code()}: $errorDetails")
                    }
                }

                override fun onFailure(call: Call<AgendamentoCliResponse>, t: Throwable) {
                    mostrarAlertaSimples("Erro na comunicação: ${t.localizedMessage ?: "Erro desconhecido"}")
                }
            })
    }
}
