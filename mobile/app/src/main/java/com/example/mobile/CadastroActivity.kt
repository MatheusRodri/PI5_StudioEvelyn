package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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

class CadastroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)

        var fistNameEditText = findViewById<EditText>(R.id.txtFirstName)
        var lastNameEditText = findViewById<EditText>(R.id.txtLastName)
        var cpfEditText = findViewById<EditText>(R.id.txtCPF)
        var emailEditText = findViewById<EditText>(R.id.txtEmailNew)
        var passwordEditText = findViewById<EditText>(R.id.txtSenhaNew)
        var passwordConfirmeEditText = findViewById<EditText>(R.id.txtSenhaNewConfirme)
        var btnCadastrar = findViewById<Button>(R.id.btnSingup)


        btnCadastrar.setOnClickListener(){
            var txtFirstName = fistNameEditText.text.toString().trim()
            var txtLastName = lastNameEditText.text.toString().trim()
            var txtCPF = cpfEditText.text.toString().trim()
            var txtEmail = emailEditText.text.toString().trim()
            var txtPassword = passwordEditText.text.toString().trim()
            var txtPasswordConfirme = passwordConfirmeEditText.text.toString().trim()

            if(txtFirstName.length <= 0 || txtLastName.length <= 0 || txtCPF.length <= 0 || txtEmail.length <= 0 || txtPassword.length <= 0 || txtPasswordConfirme.length <= 0 ){
                mostrarAlertaSimples("Todos os campos precisam ser preenchidos")
            }

            if(txtPassword != txtPasswordConfirme){
                mostrarAlertaSimples("As senha não são iguais")
            }else{
                handleCadastro(txtFirstName,txtLastName,txtCPF,txtEmail,txtPassword)
            }

        }
    }

    private fun mostrarAlertaSimples(mensagem:String) {
        // 1. Crie o Builder
        val builder = AlertDialog.Builder(this) // 'this' refere-se ao Context (sua Activity)

        // 2. Configure o diálogo
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

    private fun mostrarAlertaCadastro(mensagem:String) {
        // 1. Crie o Builder
        val builder = AlertDialog.Builder(this) // 'this' refere-se ao Context (sua Activity)

        // 2. Configure o diálogo
        builder.setTitle("") // Define o título (opcional)
        builder.setMessage(mensagem) // Define a mensagem

        // 3. Configure os botões (pelo menos um é recomendado)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss() // Fecha o diálogo explicitamente se necessário
            startActivity(Intent(this,MainActivity::class.java))
        }

        // 4. Crie o AlertDialog a partir do Builder
        val dialog: AlertDialog = builder.create()

        // 5. Exiba o diálogo
        dialog.show()
    }


    private fun handleCadastro(firstName:String,lastName:String,cpf:String,email:String,password:String){
        var nome = firstName + " " + lastName
        val request = CadatroRequest(cpf,nome,email,password)
        Log.d("API_REQUEST", "Enviando para /cliente: ${request}") // Log é útil

        val call = NetworkConfigCadastro.apiService.cadastro(request) // Usa a interface corrigida

        // REMOVA O List<> daqui
        call.enqueue(object: Callback<CadatroResponse>{
            override fun onResponse(
                // REMOVA O List<> daqui
                call: Call<CadatroResponse>,
                response: Response<CadatroResponse> // REMOVA O List<> daqui
            ){
                if(response.isSuccessful && response.body() != null){
                    // response.body() agora é o objeto CadatroResponse com o campo 'message'
                    handleCadastroSuccess(response.body()!!)
                }else{
                    // Tratamento de erro (bom logar o errorBody aqui também)
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
                    mostrarAlertaSimples("Erro ${response.code()} ao cadastrar. Detalhes: $errorDetails")
                }
            }
            override fun onFailure(
                // REMOVA O List<> daqui
                call: Call<CadatroResponse>,
                t: Throwable
            ) {
                Log.e("API_CALL_FAILURE", "Falha na chamada da API: ${t.message}", t)
                mostrarAlertaSimples("Falha na comunicação: ${t.localizedMessage ?: "Erro desconhecido"}")
            }
        })
    }
    // Recebe o CadatroResponse que contém a 'message'
    private fun handleCadastroSuccess(cadatroResponse: CadatroResponse){
        // Não precisa mais de isNotEmpty() ou .first()
        // Acessa diretamente a propriedade 'message' do objeto recebido
        mostrarAlertaCadastro("${cadatroResponse.message}")

    }
}

object NetworkConfigCadastro {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    val apiService: ApiServiceCadatro by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceCadatro::class.java)
    }
}

interface ApiServiceCadatro {
    @POST("cliente")
    fun cadastro(@Body request: CadatroRequest): Call<CadatroResponse>
}


data class CadatroRequest(
    val CPF: String,
    val NOME: String,
    val EMAIL: String,
    val SENHA: String
)

data class CadatroResponse(
    val message : String
)

