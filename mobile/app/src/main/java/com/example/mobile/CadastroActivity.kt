package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.data.model.cadastro.CadastroRequest
import com.example.mobile.data.model.cadastro.CadastroResponse
import com.example.mobile.data.remote.provider.CadastroProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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

            if(txtFirstName.isEmpty() || txtLastName.isEmpty() || txtCPF.isEmpty() || txtEmail.isEmpty() || txtPassword.isEmpty() || txtPasswordConfirme.isEmpty()){

                Toast.makeText(this,"Todos os campos precisam ser preenchidos",Toast.LENGTH_LONG).show()

            }

            if(txtPassword != txtPasswordConfirme){

                Toast.makeText(this,"As senha não são iguais",Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }
            handleCadastro(txtFirstName,txtLastName,txtCPF,txtEmail,txtPassword)
        }

    }


    private fun handleCadastro(firstName:String,lastName:String,cpf:String,email:String,password:String){

        val nome = "$firstName $lastName"
        val request = CadastroRequest(cpf,nome,email,password)

        val call =  CadastroProvider.cadastroApi.cadastro(request)


        call.enqueue(object: Callback<CadastroResponse>{
            override fun onResponse(

                call: Call<CadastroResponse>,
                response: Response<CadastroResponse>

            ){
                if(response.isSuccessful && response.body() != null){

                    handleCadastroSuccess(response.body()!!)

                }else{


                    try {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrBlank()) {
                            Log.e("erro_body", errorBody)
                        }
                    } catch (e: Exception) {
                        Log.e("API_RESPONSE_ERROR", "Erro ao ler errorBody: ${e.message}")
                    }

                }
            }
            override fun onFailure(

                call: Call<CadastroResponse>,
                t: Throwable
            ) {
                Log.e("API_CALL_FAILURE", "Falha na chamada da API: ${t.message}", t)

            }
        })
    }

    private fun handleCadastroSuccess(cadastroResponse: CadastroResponse){

        Toast.makeText(this, cadastroResponse.message,Toast.LENGTH_LONG).show()
        val intensao = Intent(this,MainActivity::class.java)
        startActivity(intensao)

    }
}
