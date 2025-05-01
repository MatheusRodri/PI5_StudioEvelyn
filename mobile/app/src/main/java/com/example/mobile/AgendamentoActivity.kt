package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log // Mantido para logs de depuração
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton // Importação necessária para RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog // Para exibir alertas ao usuário
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Immutable // Mantido caso usado em conjunto com Compose
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
// Removido: import java.sql.Time // Não é mais necessário
// Removido: import java.sql.Date // Não é mais necessário
import java.text.ParseException // Importação para tratar erros de parse
import java.text.SimpleDateFormat // Importação para formatar/parsear datas/horas
import java.util.Locale // Importação para definir o Locale para formatação
import java.util.Date // Importação necessária para java.util.Date

// Data class Servico - Representa um serviço com nome e preço
@Immutable // Boa prática, indica que o objeto é imutável após a criação
data class Servico(
    val name: String,
    val price: Double
)

// Lista COMPLETA de serviços disponíveis - Mantida conforme o original
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

// --- Configuração de Rede e Data Classes para AGENDAMENTO ---

object NetworkConfigAgendamento {
    // Garanta que este IP esteja correto para sua configuração de emulador/dispositivo
    // 10.0.2.2 é tipicamente usado pelo Emulador Android para acessar o localhost da máquina hospedeira
    private const val BASE_URL = "http://10.0.2.2:5000/" // URL base da sua API

    // Criação preguiçosa (lazy) da instância do serviço Retrofit
    val apiService: ApiServiceAgendamentoCli by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Define a URL base
            .addConverterFactory(GsonConverterFactory.create()) // Adiciona o conversor Gson para JSON
            .build() // Constrói a instância do Retrofit
            .create(ApiServiceAgendamentoCli::class.java) // Cria a implementação da interface do serviço
    }
}

// Interface que define os endpoints da API para Agendamento
interface ApiServiceAgendamentoCli {
    @POST("agendamentos")
    fun criarAgendamento(@Body request: AgendamentoRequest): Call<AgendamentoCliResponse> // Define o método da API
}

// Data class para o corpo da requisição de AGENDAMENTO
// *** Os nomes dos campos (DATA, HORA, etc.) devem corresponder EXATAMENTE ao que a API espera ***
data class AgendamentoRequest(
    val DATA: String, // DATA como String formatada
    val HORA: String, // HORA como String formatada
    val PROCEDIMENTO: String,
    val VALOR: Double,
    val TP_PAGAMENTO: String,
    val ID_CLIENT: Int
)

// Data class para o corpo da resposta da API de AGENDAMENTO
data class AgendamentoCliResponse(
    val message: String // Assumindo que a API responde com uma mensagem simples
)

// --- Activity de Agendamento ---

class AgendamentoActivity : AppCompatActivity() {

    // Declaração das Views (componentes de UI)
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var radioGroupPagamento: RadioGroup
    private lateinit var checkBoxBrasileiro: CheckBox
    private lateinit var checkBoxCombo: CheckBox
    private lateinit var checkBoxDesign: CheckBox
    private lateinit var btnAgendar: Button

    private var idCliente: Int? = null // Variável para armazenar o ID do cliente (agora Int?)

    // Formatadores para LER a entrada do usuário
    private val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val inputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Formatadores para FORMATAR a saída para a API (AJUSTE OS PADRÕES CONFORME NECESSÁRIO!)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val apiTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()) // Ex: "HH:mm" ou "HH:mm:ss"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita layout de ponta a ponta (Edge-to-Edge)
        setContentView(R.layout.activity_agendamento) // Define o layout XML para esta activity

        // --- Recuperação Segura do ID do Cliente (Int) ---
        // Log.e("AgendamentoActivity", idCliente.toString()) // Log inicial mostrará null, pode remover se não for útil
        val idClienteStr: String? = intent.getStringExtra("ID") // Pega como String?
        if (idClienteStr.isNullOrBlank()) {
            Log.e("AgendamentoActivity", "ID do Cliente (String) não encontrado ou vazio nos extras do Intent.")
            mostrarAlertaSimples("Erro: Identificação do cliente não fornecida.")
            // Considerar finalizar a activity ou usar um valor padrão se aplicável
            // finish()
        } else {
            // Tenta converter a String para Int de forma segura
            idCliente = idClienteStr.toIntOrNull()
            if (idCliente == null) {
                Log.e("AgendamentoActivity", "Falha ao converter ID do Cliente ('$idClienteStr') para Int.")
                mostrarAlertaSimples("Erro: Identificação do cliente inválida.")
                // Considerar finalizar a activity
                // finish()
            } else {
                Log.d("AgendamentoActivity", "ID do Cliente (Int) recebido: $idCliente")
            }
        }
        // ----------------------------------------------------


        // Inicializa as Views encontrando-as pelo ID no layout XML
        editTextDate = findViewById(R.id.editData)
        editTextTime = findViewById(R.id.editHoraAtualiza)
        radioGroupPagamento = findViewById(R.id.radioGroup)
        checkBoxBrasileiro = findViewById(R.id.chkBrasileiro)
        checkBoxCombo = findViewById(R.id.chkCombo)
        checkBoxDesign = findViewById(R.id.chkDesign)
        btnAgendar = findViewById(R.id.idBtnAgendarAtualizar)

        // Configura o listener de clique para o botão "Agendar"
        btnAgendar.setOnClickListener {
            // 1. Valida o ID do Cliente (agora checa se é null)
            if (idCliente == null) { // Correção na validação do ID
                mostrarAlertaSimples("Erro: Não foi possível identificar o cliente. Tente novamente.")
                return@setOnClickListener // Para a execução do listener aqui
            }

            // 2. Coleta os dados do formulário (Strings)
            val dataStr = editTextDate.text.toString().trim()
            val horaStr = editTextTime.text.toString().trim()
            val nomesProcedimentosSelecionados = obterNomesProcedimentosSelecionadosDoXml()
            val precoTotal = calcularPrecoTotal(nomesProcedimentosSelecionados)

            // 3. Obtém a forma de pagamento selecionada (String)
            val selectedPaymentId = radioGroupPagamento.checkedRadioButtonId
            val formaPagamento: String

            if (selectedPaymentId != -1) {
                val selectedRadioButton = findViewById<RadioButton>(selectedPaymentId)
                formaPagamento = selectedRadioButton.text.toString()
            } else {
                mostrarAlertaSimples("Por favor, selecione uma forma de pagamento.")
                return@setOnClickListener
            }

            // 4. Validação básica dos campos String e lista
            if (dataStr.isEmpty()) {
                mostrarAlertaSimples("Por favor, insira a data (dd/MM/yyyy).")
                return@setOnClickListener
            }
            if (horaStr.isEmpty()) {
                mostrarAlertaSimples("Por favor, insira a hora (HH:mm).")
                return@setOnClickListener
            }
            if (nomesProcedimentosSelecionados.isEmpty()) {
                mostrarAlertaSimples("Por favor, selecione pelo menos um procedimento.")
                return@setOnClickListener
            }

            // --- 5. Parse da Entrada e Formatação para API ---
            val dataFormatadaApi: String
            val horaFormatadaApi: String
            val procedimentosStr: String = nomesProcedimentosSelecionados.joinToString(separator = ",") // Junta a lista numa String

            try {
                // Parse da entrada do usuário para objeto java.util.Date
                val utilDate: Date? = inputDateFormat.parse(dataStr)
                val utilTime: Date? = inputTimeFormat.parse(horaStr) // Parse da hora também retorna Date

                // Verifica se o parse foi bem sucedido
                if (utilDate == null || utilTime == null) {
                    throw ParseException("Erro ao parsear data ou hora (resultado nulo)", 0)
                }

                // Formata os objetos Date para as Strings no formato da API
                dataFormatadaApi = apiDateFormat.format(utilDate)
                horaFormatadaApi = apiTimeFormat.format(utilTime) // Formata a parte da hora do objeto Date

                Log.d("AgendamentoActivity", "Data formatada para API: $dataFormatadaApi")
                Log.d("AgendamentoActivity", "Hora formatada para API: $horaFormatadaApi")

            } catch (e: ParseException) {
                Log.e("AgendamentoActivity", "Erro ao parsear/formatar data ou hora: ${e.message}")
                mostrarAlertaSimples("Formato inválido. Use dd/MM/yyyy para data e HH:mm para hora.")
                return@setOnClickListener // Para a execução se o formato estiver incorreto
            }

            // Cria o objeto AgendamentoRequest com Strings formatadas
            val agendamentoRequest = AgendamentoRequest(
                DATA = dataFormatadaApi,       // Passa a String formatada (ex: "2025-04-12")
                HORA = horaFormatadaApi,       // Passa a String formatada (ex: "18:14:00")
                PROCEDIMENTO = procedimentosStr,
                VALOR = precoTotal,
                TP_PAGAMENTO = formaPagamento,
                ID_CLIENT = idCliente!!        // Passa o Int (!! é seguro após a validação inicial)
            )
            // -----------------------------------------------------------


            // 6. Chama a função que faz a requisição para a API
            criarAgendamentoApi(agendamentoRequest)
        }
    }

    // --- Funções Auxiliares ---

    // Retorna uma lista de nomes dos serviços selecionados com base nos CheckBoxes existentes no XML
    private fun obterNomesProcedimentosSelecionadosDoXml(): List<String> {
        val nomesSelecionados = mutableListOf<String>()
        if (checkBoxBrasileiro.isChecked) {
            nomesSelecionados.add(checkBoxBrasileiro.text.toString())
        }
        if (checkBoxCombo.isChecked) {
            nomesSelecionados.add(checkBoxCombo.text.toString())
        }
        if (checkBoxDesign.isChecked) {
            nomesSelecionados.add(checkBoxDesign.text.toString())
        }
        return nomesSelecionados
    }

    // Calcula o preço total com base em uma lista de nomes de serviços selecionados
    private fun calcularPrecoTotal(nomesSelecionados: List<String>): Double {
        var precoTotal = 0.0
        nomesSelecionados.forEach { nomeSelecionado ->
            val servicoEncontrado = servicosList.find { it.name.equals(nomeSelecionado, ignoreCase = true) }
            precoTotal += servicoEncontrado?.price ?: 0.0
        }
        Log.d("AgendamentoActivity", "Preço Total Calculado: $precoTotal")
        return precoTotal
    }

    // --- Funções de Alerta (AlertDialog) ---

    // Exibe um diálogo de alerta simples com uma mensagem
    private fun mostrarAlertaSimples(mensagem: String) {
        AlertDialog.Builder(this)
            .setTitle("Aviso")
            .setMessage(mensagem)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Exibe uma mensagem de sucesso e navega para a MainActivity
    private fun mostrarAlertaSucessoAgendamento(mensagem: String) {
        AlertDialog.Builder(this)
            .setTitle("Sucesso")
            .setMessage(mensagem)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                // finish() // Descomente se quiser fechar esta activity após o sucesso
            }
            .setCancelable(false)
            .create()
            .show()
    }


    // --- Função de Chamada da API ---

    // Realiza a chamada à API para criar um novo agendamento
    private fun criarAgendamentoApi(request: AgendamentoRequest) {
        Log.d("API_REQUEST", "Enviando para /agendamentos: $request") // Log da requisição enviada

        NetworkConfigAgendamento.apiService.criarAgendamento(request)
            .enqueue(object : Callback<AgendamentoCliResponse> {
                override fun onResponse(
                    call: Call<AgendamentoCliResponse>,
                    response: Response<AgendamentoCliResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        Log.i("API_RESPONSE_SUCCESS", "Agendamento criado: ${responseBody.message}")
                        mostrarAlertaSucessoAgendamento("Agendamento realizado com sucesso!\n${responseBody.message}")
                    } else {
                        var errorDetails = "Nenhum detalhe no corpo do erro."
                        try {
                            val errorBody = response.errorBody()?.string()
                            if (!errorBody.isNullOrBlank()) {
                                errorDetails = errorBody
                            }
                        } catch (e: Exception) {
                            Log.e("API_RESPONSE_ERROR", "Erro ao ler errorBody: ${e.message}")
                        }
                        val errorMsg = "Erro ${response.code()}: ${response.message()}\nDetalhes: $errorDetails"
                        Log.e("API_RESPONSE_ERROR", errorMsg)
                        mostrarAlertaSimples("Falha ao agendar (Erro ${response.code()}). Detalhes: $errorDetails")
                    }
                }

                override fun onFailure(call: Call<AgendamentoCliResponse>, t: Throwable) {
                    Log.e("API_CALL_FAILURE", "Falha na chamada da API: ${t.message}", t)
                    mostrarAlertaSimples("Falha na comunicação com o servidor: ${t.localizedMessage ?: "Erro desconhecido"}")
                }
            })
    }
}