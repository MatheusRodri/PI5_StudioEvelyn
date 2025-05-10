package com.example.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile.data.local.PreferenceHelper
import com.example.mobile.data.model.agendamentos.AgendamentosRequest
import com.example.mobile.data.remote.provider.AgendamentosProvider
import com.example.mobile.databinding.ActivityAgendamentosBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AgendamentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendamentosBinding
    private lateinit var adapterAgendamento: AdapterAgendamento
    private lateinit var clienteId: String
    private lateinit var clienteCpf: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAgendamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)


        clienteCpf = PreferenceHelper.cpf.toString()
        clienteId = PreferenceHelper.idCliente.toString()

        setupWhatsAppButton()
        Log.d("AgendamentosActivity", "CPF Recebido: $clienteCpf\nID cliente: $clienteId")

        initRecyclerView()
        setupAgendarButton()
        setupLogout()
        fetchAgendamentosFromApi()
    }


    private fun setupLogout(){
        binding.btnLogout.setOnClickListener{
            val intensao = Intent(this,MainActivity::class.java)
            startActivity(intensao)
            finish()
        }
    }



    private fun setupWhatsAppButton() {
        binding.btnWhatsApp.setOnClickListener {
            val numero = "5511947792884"
            val url = "https://wa.me/$numero"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Não foi possível abrir o WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAgendarButton() {
        binding.btnAgendar.setOnClickListener {
            val intent = Intent(this, AgendamentoActivity::class.java)
            intent.putExtra("ID", clienteId)
            startActivity(intent)
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

        val request = AgendamentosRequest(clienteCpf)

        lifecycleScope.launch {
            try {
                val response = AgendamentosProvider.agendamentosApi.getagendamentos(request)
                adapterAgendamento.updateData(response)
                binding.progressBar.isVisible = false
                binding.idAgendamentos.isVisible = true
                binding.txtSemAgendamentos.isVisible = response.isEmpty()

            } catch (e: IOException) {
                Log.e("AgendamentosActivity", "Network Error: ${e.message}", e)
                showErrorState("Erro de rede: Verifique sua conexão")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AgendamentosActivity", "HTTP Error: ${e.code()} - ${e.message()}")
                Log.e("AgendamentosActivity", "Error Body: $errorBody")
                showErrorState("Erro ao buscar dados: ${e.code()}")
            } catch (e: Exception) {
                Log.e("AgendamentosActivity", "Unexpected Error: ${e.message}", e)
                showErrorState("Ocorreu um erro inesperado")
            }
        }
    }

    private fun showErrorState(message: String) {
        binding.progressBar.isVisible = false
        binding.idAgendamentos.isVisible = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        adapterAgendamento.updateData(emptyList())
        binding.txtSemAgendamentos.isVisible = true
    }

    override fun onResume() {
        super.onResume()
        fetchAgendamentosFromApi()
    }
}
