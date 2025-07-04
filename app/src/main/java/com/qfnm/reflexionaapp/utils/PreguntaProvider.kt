package com.qfnm.reflexionaapp.utils

import android.content.Context
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper
import com.qfnm.reflexionaapp.modelo.Pregunta
import org.json.JSONObject

object PreguntaProvider {

    fun cargarPreguntas(context: Context): List<Pregunta> {
        val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        val cantidad = prefs.getInt("cantidadPreguntas", 3)
        val categoriasElegidas = prefs.getStringSet("categoriasElegidas", emptySet()) ?: emptySet()

        // Leer archivo JSON desde assets
        val input = context.assets.open("preguntas.json")
        val json = input.bufferedReader().use { it.readText() }
        val objetoJson = JSONObject(json)

        val listaTotal = mutableListOf<Pregunta>()

        for (categoria in categoriasElegidas) {
            if (objetoJson.has(categoria)) {
                val array = objetoJson.getJSONArray(categoria)
                for (i in 0 until array.length()) {
                    val texto = array.getString(i)
                    listaTotal.add(Pregunta(texto, categoria))
                }
            }
        }

        return listaTotal.shuffled().take(cantidad)
    }
}