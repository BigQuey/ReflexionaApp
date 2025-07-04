package com.qfnm.reflexionaapp.diario

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper

class NotaAdapter(
    private val context: Context,
    private val lista: List<String>, // cada item es una fecha
    private val dbHelper: AppDatabaseHelper
) : ArrayAdapter<String>(context, 0, lista) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val fecha = lista[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_nota, parent, false)

        val tvFecha = view.findViewById<TextView>(R.id.tvFecha)
        val tvResumen = view.findViewById<TextView>(R.id.tvResumen)

        val notaCompleta = dbHelper.obtenerDiario(fecha)
        val resumen = notaCompleta?.take(80)

        tvFecha.text = fecha
        tvResumen.text = resumen?.ifBlank { "Sin contenido..." }

        return view
    }
}