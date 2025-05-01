package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast // Import Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible // Import for easy visibility toggle
import androidx.lifecycle.lifecycleScope // Import for coroutines
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile.databinding.ActivityAgendamentosBinding
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch // Import launch
import retrofit2.HttpException // Import for HTTP errors
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException

data class AgendamentoResponse(
    val ID: Int,
    val NOME: String?,
    val EMAIL: String?,
    val CPF: String?,
    val DATA: String?, // <<< MUDADO para String?
    val HORA: String?, // <<< MUDADO para String?
    val VALOR: Float,
    val PROCEDIMENTO: String?,
    val TP_PAGAMENTO: String?,
    val ID_AGENDAMENTO: Int,
    val ID_CLIENTE: Int
)

// Request: CORRIGIDO para ENVIAR "CPF" (maiúsculo) no JSON
data class AgendamentoRequestBody(
    // Diz ao Gson para usar a chave "CPF" no JSON ao serializar esta classe
    @SerializedName("CPF")
    val cpf: String // O nome da variável Kotlin pode continuar minúsculo (convenção)
)

// --- API Interface (sem alterações) ---
interface ApiServiceAgendamento {
    @POST("/agendamentos/cliente")
    suspend fun getAgendamentos(@Body requestBody: AgendamentoRequestBody): List<AgendamentoResponse>
}

// --- Retrofit Instance (sem alterações) ---
object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:5000/"
    val api: ApiServiceAgendamento by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceAgendamento::class.java)
    }
}

// --- Activity (sem alterações em relação à última versão com CPF) ---
class AgendamentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendamentosBinding
    private lateinit var adapterAgendamento: AdapterAgendamento // Seu adapter externo
    private var clienteCpf: String? = null
    private var clienteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAgendamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clienteCpf = intent.getStringExtra("CPF")
        clienteId = intent.getStringExtra("ID_Cliente")

        Log.d("AgendamentosActivity", "CPF Recebido: $clienteCpf")

        if (clienteCpf.isNullOrEmpty()) {
            Toast.makeText(this, "Erro: CPF do cliente não encontrado ou inválido na Intent.", Toast.LENGTH_LONG).show()
            Log.e("AgendamentosActivity", "CPF recebido é nulo ou vazio.")
            finish()
            return
        }

        initRecyclerView()
        setupAgendarButton()
        fetchAgendamentosFromApi()
    }

    private fun setupAgendarButton() {
        binding.btnAgendar.setOnClickListener {
            val intensao = Intent(this, AgendamentoActivity::class.java)
            Log.e("AgendamentosActivityEnvia", clienteId.toString())
            intensao.putExtra("ID", clienteId)
            startActivity(intensao)
        }
    }

    private fun initRecyclerView() {
        adapterAgendamento = AdapterAgendamento(emptyList())
        binding.idAgendamentos.layoutManager = LinearLayoutManager(this)
        binding.idAgendamentos.setHasFixedSize(true)
        binding.idAgendamentos.adapter = adapterAgendamento
    }

    private fun fetchAgendamentosFromApi() {
        binding.progressBar.isVisible = true
        binding.idAgendamentos.isVisible = false

        // Cria o RequestBody usando a variável 'cpf' (minúscula)
        // A anotação @SerializedName("CPF") cuidará para que o JSON gerado tenha a chave maiúscula
        val requestBody = AgendamentoRequestBody(cpf = clienteCpf!!)

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getAgendamentos(requestBody)
                adapterAgendamento.updateData(response)
                binding.progressBar.isVisible = false
                binding.idAgendamentos.isVisible = true

                binding.txtSemAgendamentos.isVisible = response.isEmpty()


            } catch (e: IOException) {
                Log.e("AgendamentosActivity", "Network Error: ${e.message}", e)
                binding.progressBar.isVisible = false
                binding.idAgendamentos.isVisible = true
                Toast.makeText(this@AgendamentosActivity, "Erro de rede: Verifique sua conexão", Toast.LENGTH_LONG).show()
                adapterAgendamento.updateData(emptyList())
                binding.txtSemAgendamentos.isVisible = true


            } catch (e: HttpException) {
                Log.e("AgendamentosActivity", "HTTP Error: ${e.code()} - ${e.message()}", e)
                // Log do corpo do erro, se disponível (útil para debug)
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AgendamentosActivity", "Error Body: $errorBody")
                binding.progressBar.isVisible = false
                binding.idAgendamentos.isVisible = true
                Toast.makeText(this@AgendamentosActivity, "Erro ao buscar dados: ${e.code()}", Toast.LENGTH_LONG).show()
                adapterAgendamento.updateData(emptyList())
                binding.txtSemAgendamentos.isVisible = true


            } catch (e: Exception) { // Inclui possíveis erros de conversão do Gson (ex: Date/Time)
                Log.e("AgendamentosActivity", "Unexpected Error: ${e.message}", e)
                binding.progressBar.isVisible = false
                binding.idAgendamentos.isVisible = true
                Toast.makeText(this@AgendamentosActivity, "Ocorreu um erro inesperado", Toast.LENGTH_LONG).show()
                adapterAgendamento.updateData(emptyList())
                binding.txtSemAgendamentos.isVisible = true

            }
        }
    }
    override fun onResume() {
        super.onResume()
        fetchAgendamentosFromApi() // <- sua função que recarrega os dados
    }
}