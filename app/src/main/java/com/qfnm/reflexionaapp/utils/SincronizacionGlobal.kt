package com.qfnm.reflexionaapp.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper

fun sincronizarFirestoreASQLite(
    context: Context,
    onFinalizado: (() -> Unit)? = null
) {
    val dbHelper = AppDatabaseHelper(context)
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    // 1. Descargar DIARIO desde Firestore
    firestore.collection("diarios")
        .document(uid)
        .collection("entradas")
        .get()
        .addOnSuccessListener { diarios ->
            for (doc in diarios.documents) {
                val fecha = doc.id
                val texto = doc.getString("texto") ?: continue
                dbHelper.guardarDiario(fecha, texto)
            }

            // 2. Descargar RESPUESTAS desde Firestore
            firestore.collection("respuestas")
                .document(uid)
                .collection("dias")
                .get()
                .addOnSuccessListener { respuestas ->
                    for (doc in respuestas.documents) {
                        val fecha = doc.id
                        val lista = doc.get("respuestas") as? List<Map<String, Any>> ?: continue

                        // Convertir a lista de Pair<pregunta, respuesta>
                        val respuestasFormateadas = lista.mapNotNull { item ->
                            val pregunta = item["pregunta"]?.toString()
                            val respuesta = item["respuesta"]?.toString()
                            if (pregunta != null && respuesta != null) {
                                pregunta to respuesta
                            } else null
                        }

                        dbHelper.guardarRespuestas(fecha, "", respuestasFormateadas)
                    }

                    // Marcar como sincronizado
                    context.getSharedPreferences("config", Context.MODE_PRIVATE)
                        .edit().putBoolean("sincronizacionHecha", true).apply()

                    onFinalizado?.invoke()
                }
        }
}