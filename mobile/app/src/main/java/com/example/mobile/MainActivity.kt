package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        var emailEditText = findViewById<EditText>(R.id.txtEmail)
        var passwordEditText = findViewById<EditText>(R.id.txtPassword)
        var cadastroEditText = findViewById<TextView>(R.id.txtSemLogin)
        var entrarBtn = findViewById<Button>(R.id.btnJoin)

        cadastroEditText.setOnClickListener(){
            var intensao = Intent(this,CadastroActivity::class.java)
            startActivity(intensao)
        }


        entrarBtn.setOnClickListener(){
            var emailText = emailEditText.text.toString().trim()
            var passwordText = passwordEditText.text.toString().trim()
            Log.d(TAG,emailText + passwordText)
            if (emailText.length <= 0 || passwordText.length <= 0){
                mostrarAlertaSimples("Os campos de E-mail ou Senha estão vazios")
            }else{
                handleLogin(emailText,passwordText)
            }
        }


    }

    private fun mostrarAlertaSimples(mensagem:String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("") // Define o título (opcional)
        builder.setMessage(mensagem) // Define a mensagem

        // 3. Configure os botões (pelo menos um é recomendado)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss() // Fecha o diálogo explicitamente se necessário
        }

        // 4. Crie o AlertDialog a partir do Builder
        val dialog: AlertDialog = builder.create()

        // 5. Exiba o diálogo
        dialog.show()
    }

    private fun handleLogin(email:String,password:String){
        Log.d(TAG, "Inicio do processo login: \n email: ${email} \n senha: ${password}")
        val request = LoginRequest(email, password)
        Log.d(TAG, "Inicio do processo login 2: \n variavel request ${request}")

        try {
            Log.d(TAG, "Tentando criar a chamada Retrofit...")
            val call = NetworkConfig.apiService.login(request)
            // Se chegou aqui, a criação funcionou. Logue a URL para ter certeza.
            Log.d(TAG, "Inicio do processo login 3 (Chamada criada): URL = ${call.request().url()}")

            // Continue com o enqueue
            call.enqueue(object : Callback<List<LoginResponse>> {
                override fun onResponse(
                    call: Call<List<LoginResponse>>,
                    response: Response<List<LoginResponse>>
                ) {
                    Log.d(TAG, "Callback onResponse chamado. Código: ${response.code()}")
                    if (response.isSuccessful && response.body() != null) {
                        handleLoginSuccess(response.body()!!)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Erro 500 recebido. Corpo do erro: $errorBody")
                    //handleLoginError(response.code()) // Passar response.message() pode ser útil
                    }
                }

                override fun onFailure(call: Call<List<LoginResponse>>, t: Throwable) {
                    Log.e(TAG, "Callback onFailure chamado.", t) // Logar a exceção inteira
                    handleNetworkError(t.message ?: "Erro de rede desconhecido")
                }
            })

        } catch (e: Exception) {
            // Se entrou aqui, a linha "val call = ..." falhou!
            Log.e(TAG, "==== ERRO AO CRIAR A CHAMADA RETROFIT! ====", e)
            mostrarAlertaSimples("Erro interno ao preparar a requisição: ${e.localizedMessage}")
        }
    }

    private fun handleLoginSuccess(loginResponses: List<LoginResponse>) {
        Log.d(TAG, "Inicio do processo login 5: \n AQUI ESTÁ RUIM")
        if (loginResponses.isNotEmpty()) {
            val usuarioLogado: LoginResponse = loginResponses.first()


            var intensao = Intent(this, AgendamentosActivity::class.java)
            Log.d("Login", usuarioLogado.ID.toString())
            intensao.putExtra("CPF",usuarioLogado.CPF)
            intensao.putExtra("ID_Cliente",usuarioLogado.ID.toString())
            startActivity(intensao)
            finish()
        } else {
            mostrarAlertaSimples("Usuário ou senha inválidos")
        }
    }

    private fun handleLoginError(errorCode: Int) {
        Log.e(TAG, "Erro no login: $errorCode")
        Toast.makeText(this, "Erro no login: $errorCode", Toast.LENGTH_LONG).show()
    }

    private fun handleNetworkError(errorMessage: String) {
        Log.e(TAG, "Erro de rede: $errorMessage")
        Toast.makeText(this, "Erro de rede: $errorMessage", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}

object NetworkConfig {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<List<LoginResponse>>
}

data class LoginRequest(
    val EMAIL: String,
    val SENHA: String
)

data class LoginResponse(
    val ID: Int,
    val NOME: String,
    val EMAIL: String,
    val SENHA: String,
    val CPF: String
)