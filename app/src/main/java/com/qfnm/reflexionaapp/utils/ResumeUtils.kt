package com.qfnm.reflexionaapp.utils

import com.google.gson.Gson


    fun formatearRespuestasComoJson(respuestasPorFecha: Map<String, List<Map<String, String>>>): String {
        val lista = mutableListOf<Map<String, String>>()

        respuestasPorFecha.forEach { (fecha, respuestas) ->
            respuestas.forEach { item ->
                lista.add(
                    mapOf(
                        "fecha" to fecha,
                        "categoria" to item["categoria"].orEmpty(),
                        "pregunta" to item["pregunta"].orEmpty(),
                        "respuesta" to item["respuesta"].orEmpty()
                    )
                )
            }
        }

        val resultado = mapOf("respuestas" to lista)
        return Gson().toJson(resultado)
    }
