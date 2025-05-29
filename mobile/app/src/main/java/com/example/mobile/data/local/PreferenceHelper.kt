package com.example.mobile.data.local

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {
    private const val PREF_NAME = "APP_PREFS"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var cpf: String?
        get() = prefs.getString("CPF", null)
        set(value) = prefs.edit().putString("CPF", value).apply()

    // você pode adicionar outros dados aqui também
    var idCliente: Int
        get() = prefs.getInt("ID_CLIENTE", -1)
        set(value) = prefs.edit().putInt("ID_CLIENTE", value).apply()

    fun removeCpf() {
        // Verifica se 'prefs' foi inicializada para evitar exceções.
        // Essencial se houver chance de chamar antes de init().
        if (::prefs.isInitialized) {
            prefs.edit().remove("CPF").apply()
        }
    }

    /**
     * Remove apenas o ID do Cliente das SharedPreferences.
     */
    fun removeIdCliente() {
        if (::prefs.isInitialized) {
            prefs.edit().remove("ID_CLIENTE").apply()
        }
    }
}
