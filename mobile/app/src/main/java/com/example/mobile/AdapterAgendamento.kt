package com.example.mobile


import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.data.model.agendamento.excluir.AgendamentoExcluirRequest
import com.example.mobile.data.model.agendamento.excluir.AgendamentoExcluirResponse
import com.example.mobile.data.model.agendamentos.AgendamentosResponse
import com.example.mobile.data.remote.provider.AgendamentoProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class AdapterAgendamento(
    private var agendamentos: List<AgendamentosResponse>
) : RecyclerView.Adapter<AdapterAgendamento.AgendamentoViewHolder>() {

    class AgendamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeAgendamento: TextView = itemView.findViewById(R.id.idNomeAgendamento)
        val dataHoraValor: TextView = itemView.findViewById(R.id.idDataHorarioValor)
        val btnExcluir: Button = itemView.findViewById(R.id.idBtnExcluir)
        val btnAlterar: Button = itemView.findViewById(R.id.idBtnAlterar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgendamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agendamento_adapter, parent, false)
        return AgendamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgendamentoViewHolder, position: Int) {
        val agendamento = agendamentos[position]

        val dataFormatada = formatarData(agendamento.DATA)
        val horaFormatada = formatarHora(agendamento.HORA)
        val valorFormatado = formatarValor(agendamento.VALOR.toDouble())

        holder.nomeAgendamento.text = agendamento.PROCEDIMENTO ?: "Procedimento não informado"
        holder.dataHoraValor.text = "Data: $dataFormatada\nHora: $horaFormatada\nValor: $valorFormatado"

        holder.btnExcluir.setOnClickListener {
            excluirAgendamentoSimples(it.context, agendamento.ID_AGENDAMENTO, position)
        }

        holder.btnAlterar.setOnClickListener {
            var intensao = Intent(it.context, Atualiza_Agendamento_Activity::class.java)
            intensao.putExtra("ID_AGENDAMENTO",agendamento.ID_AGENDAMENTO)
            intensao.putExtra("NOME_PROCEDIMENTO",agendamento.PROCEDIMENTO)
            intensao.putExtra("HORA_AGENDAMENTO",agendamento.HORA)
            intensao.putExtra("DATA_AGENDAMENTO",agendamento.DATA)
            intensao.putExtra("VALOR_TOTAL",agendamento.VALOR)
            intensao.putExtra("FORMA_PAGAMENTO_AGENDAMENTO",agendamento.TP_PAGAMENTO)
            intensao.putExtra("ID_CLIENTE",agendamento.ID_CLIENTE)


            Log.d("Atualiza","id agendamento: ${agendamento.ID_AGENDAMENTO}\nid_cliente ${agendamento.ID_CLIENTE}")

            it.context.startActivity(intensao)
        }
    }


    override fun getItemCount(): Int = agendamentos.size

    private fun formatarData(data: String?): String {
        return try {
            if (!data.isNullOrEmpty()) {
                val dateTime = OffsetDateTime.parse(data)
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
                dateTime.format(formatter)
            } else "-"
        } catch (e: Exception) {
            "-"
        }
    }

    private fun formatarHora(hora: String?): String {
        return try {
            if (!hora.isNullOrEmpty()) {
                val localTime = java.time.LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm:ss"))
                localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else "-"
        } catch (e: Exception) {
            "-"
        }
    }

    private fun formatarValor(valor: Double?): String {
        return try {
            val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            formatoMoeda.format(valor ?: 0.0)
        } catch (e: Exception) {
            "R$ --,--"
        }
    }

    private fun excluirAgendamentoSimples(context: Context, id: Int, posicao: Int) {


        val request = AgendamentoExcluirRequest(ID = id)
        val call = AgendamentoProvider.agendamentoApi.excluirAgendamento(request)

        call.enqueue(object : Callback<AgendamentoExcluirResponse> {
            override fun onResponse(
                call: Call<AgendamentoExcluirResponse>,
                response: Response<AgendamentoExcluirResponse>
            ) {
                val mensagem = response.body()?.message ?: "Agendamento excluído com sucesso!"
                Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                removerItem(posicao)
            }

            override fun onFailure(call: Call<AgendamentoExcluirResponse>, t: Throwable) {
                Toast.makeText(context, "Erro ao excluir: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }


    fun updateData(novaLista: List<AgendamentosResponse>) {
        agendamentos = novaLista
        notifyDataSetChanged()
    }

    fun removerItem(posicao: Int) {
        agendamentos = agendamentos.toMutableList().apply {
            removeAt(posicao)
        }
        notifyItemRemoved(posicao)
    }

}
