package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Immutable
import com.example.mobile.data.model.agendamento.cria.AgendamentoRequest
import com.example.mobile.data.model.agendamento.cria.AgendamentoResponse
import com.example.mobile.data.remote.provider.AgendamentoProvider
import retrofit2.*
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

        aplicarMascaraData(editTextDate)
        aplicarMascaraHora(editTextTime)

        val idClienteStr: String? = intent.getStringExtra("ID")
        idCliente = idClienteStr?.toIntOrNull()
        if (idCliente == null) {
            Log.e("Agendamento","Erro: Identificação do cliente inválida.")
            return
        }


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
        btnAgendar.isEnabled = true
        btnAgendar.text = "Agendar"

        btnAgendar.setOnClickListener {
            btnAgendar.setOnClickListener {
                val dataStr = editTextDate.text.toString().trim()
                val horaStr = editTextTime.text.toString().trim()
                val nomesProcedimentosSelecionados = obterNomesProcedimentosSelecionadosDoXml()
                val precoTotal = calcularPrecoTotal(nomesProcedimentosSelecionados)

                val selectedPaymentId = radioGroupPagamento.checkedRadioButtonId
                val formaPagamento = if (selectedPaymentId != -1) {
                    findViewById<RadioButton>(selectedPaymentId).text.toString()
                } else {
                    Toast.makeText(this,"Por favor, selecione uma forma de pagamento.",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (dataStr.isEmpty() || horaStr.isEmpty() || nomesProcedimentosSelecionados.isEmpty()) {
                    Toast.makeText(this,"Preencha todos os campos corretamente.",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val dataFormatadaApi: String
                val horaFormatadaApi: String
                val procedimentosStr = nomesProcedimentosSelecionados.joinToString(",")

                try {
                    val utilDate = inputDateFormat.parse(dataStr)
                    val utilTime = inputTimeFormat.parse(horaStr)

                    if (utilDate == null || utilTime == null) throw ParseException("Formato inválido", 0)

                    // ✅ Validação 1: Data não pode ser anterior ao dia atual
                    val hoje = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.time
                    if (utilDate.before(hoje)) {

                        Toast.makeText(this,"A data deve ser hoje ou posterior.",Toast.LENGTH_LONG).show()
                        return@setOnClickListener

                    }

                    // ✅ Validação 2: Hora entre 09:00 e 20:00
                    val hora = Calendar.getInstance().apply { time = utilTime }.get(Calendar.HOUR_OF_DAY)
                    val minuto = Calendar.getInstance().apply { time = utilTime }.get(Calendar.MINUTE)

                    if (hora < 9 || hora > 20 || (hora == 20 && minuto > 0)) {
                        Toast.makeText(this,"O horário deve estar entre 09:00 e 20:00.",Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    dataFormatadaApi = apiDateFormat.format(utilDate)
                    horaFormatadaApi = apiTimeFormat.format(utilTime)

                } catch (e: ParseException) {
                    Log.e("Agendamento","Formato inválido. Use dd/MM/yyyy e HH:mm.")
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

    private fun criarAgendamentoApi(request: AgendamentoRequest) {

        btnAgendar.isEnabled = false
        btnAgendar.text = "Aguarde..."

        val call = AgendamentoProvider.agendamentoApi.criarAgendamento(request)


        call.enqueue(object : Callback<AgendamentoResponse> {
                override fun onResponse(

                    call: Call<AgendamentoResponse>,
                    response: Response<AgendamentoResponse>

                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@AgendamentoActivity,"Agendamento realizado com sucesso!",Toast.LENGTH_LONG).show()
                        var intensao = Intent(this@AgendamentoActivity, AgendamentosActivity::class.java)

                        startActivity(intensao)
                        finish()
                    } else {
                        val errorDetails = try {
                            response.errorBody()?.string() ?: "Erro desconhecido."
                        } catch (e: Exception) {
                            "Erro ao ler erroBody: ${e.message}"
                        }
                        Log.e("Agendamento","Erro ${response.code()}: $errorDetails")
                    }
                }

                override fun onFailure(call: Call<AgendamentoResponse>, t: Throwable) {
                    Log.e("Agendamento","Erro na comunicação: ${t.localizedMessage ?: "Erro desconhecido"}")
                }
            })
    }

    fun aplicarMascaraData(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s == null) return

                val texto = s.toString().filter { it.isDigit() }
                val builder = StringBuilder()

                var i = 0
                while (i < texto.length && i < 8) {
                    builder.append(texto[i])
                    if (i == 1 || i == 3) builder.append('/')
                    i++
                }

                isUpdating = true
                editText.setText(builder.toString())
                editText.setSelection(builder.length.coerceAtMost(editText.text.length))
                isUpdating = false
            }
        })
    }

    fun aplicarMascaraHora(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s == null) return

                val texto = s.toString().filter { it.isDigit() }
                val builder = StringBuilder()

                var i = 0
                while (i < texto.length && i < 4) {
                    builder.append(texto[i])
                    if (i == 1) builder.append(':')
                    i++
                }

                isUpdating = true
                editText.setText(builder.toString())
                editText.setSelection(builder.length.coerceAtMost(editText.text.length))
                isUpdating = false
            }
        })
    }


}
