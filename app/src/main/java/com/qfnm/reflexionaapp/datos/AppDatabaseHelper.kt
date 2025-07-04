package com.qfnm.reflexionaapp.datos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qfnm.reflexionaapp.modelo.Respuesta
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null, DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla Diario
        db.execSQL(
            """CREATE TABLE diario (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT UNIQUE,
                texto TEXT
            )"""
        )

        // Crear tabla Respuesta
        db.execSQL(
            """CREATE TABLE respuesta (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT,
                categoria TEXT,
                pregunta TEXT,
                respuesta TEXT
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS diario")
        db.execSQL("DROP TABLE IF EXISTS respuesta")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "reflexiona.db"
        private const val DATABASE_VERSION = 1
    }


    fun guardarDiario(fecha: String, texto: String) {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("fecha", fecha)
            put("texto", texto)
        }
        db.insertWithOnConflict("diario", null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerDiario(fecha: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            "diario", arrayOf("texto"),
            "fecha = ?", arrayOf(fecha),
            null, null, null
        )

        var texto: String? = null
        if (cursor.moveToFirst()) {
            texto = cursor.getString(0)
        }
        cursor.close()
        return texto
    }

    // para las respuestas

    fun guardarRespuestas(fecha: String, categoria: String, respuestas: List<Pair<String, String>>) {
        val db = writableDatabase
        respuestas.forEach { (pregunta, respuesta) ->
            val valores = ContentValues().apply {
                put("fecha", fecha)
                put("categoria", categoria)
                put("pregunta", pregunta)
                put("respuesta", respuesta)
            }
            db.insert("respuesta", null, valores)
        }
    }

    fun obtenerRespuestasPorFecha(fecha: String): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.query(
            "respuesta",
            arrayOf("categoria", "pregunta", "respuesta"),
            "fecha = ?",
            arrayOf(fecha),
            null, null, null
        )

        val lista = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            val map = mapOf(
                "categoria" to cursor.getString(0),
                "pregunta" to cursor.getString(1),
                "respuesta" to cursor.getString(2)
            )
            lista.add(map)
        }
        cursor.close()
        return lista
    }
    fun obtenerRespuestasUltimos7Dias(): List<Respuesta> {
        val db = readableDatabase
        val lista = mutableListOf<Respuesta>()
        // Calcular fecha hace 7 días
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendario = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }
        val fechaLimite = formato.format(calendario.time)

        val cursor = db.rawQuery(
            """
        SELECT fecha, categoria, pregunta, respuesta
        FROM respuesta
        WHERE fecha >= ?
        ORDER BY fecha ASC
        """, arrayOf(fechaLimite)
        )

        while (cursor.moveToNext()) {
            val respuesta = Respuesta(
                pregunta = cursor.getString(2),
                respuesta = cursor.getString(3),
                fecha = cursor.getString(0),
                categoria = cursor.getString(1)
            )
            lista.add(respuesta)
        }

        cursor.close()
        return lista
    }


    fun obtenerFechasDeDiario(): List<String> {
        val db = readableDatabase
        val lista = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT fecha FROM diario ORDER BY fecha DESC", null)

        while (cursor.moveToNext()) {
            lista.add(cursor.getString(0))
        }
        cursor.close()
        return lista
    }
    // UNICA FUNCION DEL DOCUMENTO QUE INTERACTUA CON LAS 2 BD LAS OTRAS LO HACEN DIRECTAMENTE EN LA ACTIVIDAD
    fun eliminarNotaPorFecha(context: Context, fecha: String) {
        val db = writableDatabase
        db.delete("diario", "fecha = ?", arrayOf(fecha))
        db.close()

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("diarios")
            .document(uid)
            .collection("entradas")
            .document(fecha)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Eliminado también de Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Eliminado localmente, pero falló en Firestore", Toast.LENGTH_SHORT).show()
            }
    }
}