package com.example.mobile


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.OffsetDateTime
import java.util.Locale
import java.text.NumberFormat


class AdapterAgendamento(private var agendamentos: List<AgendamentoResponse>) :
    RecyclerView.Adapter<AdapterAgendamento.AgendamentoViewHolder>() {

    // 2. ViewHolder: Referências aos dois TextViews (sem alterações)
    class AgendamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TextView para mostrar o nome do procedimento (ou nome do cliente, se preferir)
        val nomeAgendameto: TextView = itemView.findViewById(R.id.idNomeAgendamento)
        // TextView para mostrar Data, Hora e Valor combinados
        val dataHoraValor: TextView = itemView.findViewById(R.id.idDataHorarioValor)
    }

    // 3. onCreateViewHolder (sem alterações)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgendamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agendamento_adapter, parent, false)
        return AgendamentoViewHolder(view)
    }

    // 4. onBindViewHolder (AJUSTADO para formatar valor e melhorar clareza)
    override fun onBindViewHolder(holder: AgendamentoViewHolder, position: Int) {
        val agendamento = agendamentos[position]

        // --- Formatação da DATA (sem alterações) ---
        val dataFormatada = try {
            if (!agendamento.DATA.isNullOrEmpty()) {
                val offsetDateTime = OffsetDateTime.parse(agendamento.DATA)
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
                offsetDateTime.format(formatter)
            } else { "-" }
        } catch (e: DateTimeParseException) {
            Log.e("AdapterAgendamento", "Erro ao parsear data: ${agendamento.DATA}", e)
            "-"
        } catch (e: Exception){
            Log.e("AdapterAgendamento", "Erro inesperado ao formatar data: ${agendamento.DATA}", e)
            "-"
        }

        // --- Formatação da HORA (sem alterações) ---
        val horaFormatada = try {
            if (!agendamento.HORA.isNullOrEmpty()) {
                val parserHora = DateTimeFormatter.ofPattern("HH:mm:ss")
                val localTime = java.time.LocalTime.parse(agendamento.HORA, parserHora)
                val formatterHora = DateTimeFormatter.ofPattern("HH:mm")
                localTime.format(formatterHora)
            } else { "-" }
        } catch (e: DateTimeParseException) {
            Log.e("AdapterAgendamento", "Erro ao parsear hora: ${agendamento.HORA}", e)
            "-"
        } catch (e: Exception){
            Log.e("AdapterAgendamento", "Erro inesperado ao formatar hora: ${agendamento.HORA}", e)
            "-"
        }

        // --- Formatação do VALOR como Moeda Brasileira (AJUSTADO) ---
        val valorFormatado = try {
            // Cria um formato de moeda para o Locale Português (Brasil)
            val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            // Formata o valor Float
            formatoMoeda.format(agendamento.VALOR)
        } catch (e: Exception) {
            Log.e("AdapterAgendamento", "Erro ao formatar valor: ${agendamento.VALOR}", e)
            "R$ --,--" // Placeholder em caso de erro
        }

        // --- Define os textos nos TextViews ---

        // Define o texto do primeiro TextView.
        // ATENÇÃO: Atualmente está mostrando o PROCEDIMENTO.
        // Se quiser mostrar o NOME do cliente, use agendamento.NOME
        holder.nomeAgendameto.text = agendamento.PROCEDIMENTO ?: "Procedimento não informado" // Placeholder ajustado

        // Combina Data, Hora e o VALOR formatado no segundo TextView, com quebras de linha
        holder.dataHoraValor.text = "Data: $dataFormatada\nHora: $horaFormatada\nValor: $valorFormatado"

        // Código comentado removido (estava redundante)
    }

    // 5. getItemCount (sem alterações)
    override fun getItemCount(): Int = agendamentos.size

    // 6. updateData (sem alterações)
    fun updateData(newAgendamentos: List<AgendamentoResponse>) {
        agendamentos = newAgendamentos
        notifyDataSetChanged()
    }
}