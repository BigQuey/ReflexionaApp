package com.qfnm.reflexionaapp.preguntas

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qfnm.reflexionaapp.R

class PreguntaAdapter(private val preguntas: List<String>) :
    RecyclerView.Adapter<PreguntaAdapter.PreguntaViewHolder>() {
    val respuestas = mutableMapOf<Int, String>()

    inner class PreguntaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPregunta: TextView = itemView.findViewById(R.id.tvPregunta)
        val etRespuesta: EditText = itemView.findViewById(R.id.etRespuesta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreguntaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pregunta, parent, false)
        return PreguntaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PreguntaViewHolder, position: Int) {
        holder.tvPregunta.text = preguntas[position]

        holder.etRespuesta.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    respuestas[currentPos] = s.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount() = preguntas.size
}