package com.example.mobile


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.data.local.PreferenceHelper
import com.example.mobile.data.model.login.LoginRequest
import com.example.mobile.data.model.login.LoginResponse
import com.example.mobile.data.remote.provider.LoginProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class MainActivity : AppCompatActivity() {

    private lateinit var btnEntrar:Button
    private lateinit var clienteId: String
    private lateinit var clienteCpf: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        val emailEditText = findViewById<EditText>(R.id.txtEmail)
        val passwordEditText = findViewById<EditText>(R.id.txtPassword)
        val cadastroEditText = findViewById<TextView>(R.id.txtSemLogin)
        btnEntrar = findViewById<Button>(R.id.btnJoin)

        PreferenceHelper.init(this)

        clienteCpf = PreferenceHelper.cpf.toString()
        clienteId = PreferenceHelper.idCliente.toString()

        Log.d("teste","${clienteId} ${clienteCpf}" )
        if (clienteId.toInt() > 0 && !clienteCpf.isNullOrEmpty()){

            val intensao = Intent(this,AgendamentosActivity::class.java)
            startActivity(intensao)
            finish()
        }

        cadastroEditText.setOnClickListener {
            val intensao = Intent(this,CadastroActivity::class.java)
            startActivity(intensao)
        }

        btnEntrar.setOnClickListener {

            val emailText = emailEditText.text.toString().trim()
            val passwordText = passwordEditText.text.toString().trim()

            Log.d(TAG,"E-mail digitado: ${emailText}\nSenha digitada: $passwordText")

            if (emailText.isEmpty() || passwordText.isEmpty()){
                Toast.makeText(this,"Os campos de E-mail ou Senha estão vazios",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            handleLogin(emailText,passwordText)
        }
    }


    private fun handleLogin(email:String,password:String){

        btnEntrar.isEnabled = false
        btnEntrar.text = "Aguarde..."

        val request = LoginRequest(email, password)

        try {

            val call = LoginProvider.loginApi.login(request)

            call.enqueue(object : Callback<List<LoginResponse>> {
                override fun onResponse(
                    call: Call<List<LoginResponse>>,
                    response: Response<List<LoginResponse>>
                ) {

                    if (response.isSuccessful && response.body() != null) {
                        handleLoginSuccess(response.body()!!)
                        btnEntrar.isEnabled = true
                        btnEntrar.text = "Entrar"
                    } else {
                        Log.d(TAG,response.errorBody().toString())

                    }
                }

                override fun onFailure(call: Call<List<LoginResponse>>, t: Throwable) {
                    Log.e(TAG, "Callback onFailure chamado.", t) // Logar a exceção inteira
                    handleNetworkError(t.message ?: "Erro de rede desconhecido")
                    btnEntrar.isEnabled = true
                    btnEntrar.text = "Entrar"
                }
            })

        } catch (e: Exception) {

            Log.e(TAG, ("Erro interno ao preparar a requisição: ${e.localizedMessage}"))

        }
    }

    private fun handleLoginSuccess(loginResponses: List<LoginResponse>) {

        if (loginResponses.isNotEmpty()) {
            val usuarioLogado: LoginResponse = loginResponses.first()

            PreferenceHelper.cpf = usuarioLogado.CPF
            PreferenceHelper.idCliente = usuarioLogado.ID

            val intensao = Intent(this, AgendamentosActivity::class.java)
            startActivity(intensao)
            finish()
        } else {
            Toast.makeText(this, "Usuário ou senha inválido", Toast.LENGTH_LONG).show()
        }
    }


    private fun handleNetworkError(errorMessage: String) {
        Log.e(TAG, "Erro de rede: $errorMessage")
        Toast.makeText(this, "Erro de rede: $errorMessage", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

}
