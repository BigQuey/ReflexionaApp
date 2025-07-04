package com.qfnm.reflexionaapp.preguntas

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreguntasResponderActivity : AppCompatActivity() {
    private lateinit var categoria: String
    private var cantidad: Int = 0

    private lateinit var adapter: PreguntaAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val fechaActual: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preguntas_responder)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        categoria = intent.getStringExtra("categoria") ?: ""
        cantidad = intent.getIntExtra("cantidad", 0)

        findViewById<TextView>(R.id.tvCategoria).text = "Categoría: $categoria"

        val preguntas = cargarPreguntasDesdeJson(categoria).shuffled().take(cantidad)

        adapter = PreguntaAdapter(preguntas)
        findViewById<RecyclerView>(R.id.rvPreguntas).apply {
            layoutManager = LinearLayoutManager(this@PreguntasResponderActivity)
            this.adapter = this@PreguntasResponderActivity.adapter
        }

        findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            guardarRespuestas(preguntas, adapter.respuestas)
        }
    }

    private fun cargarPreguntasDesdeJson(categoria: String): List<String> {
        val input = assets.open("preguntas.json")
        val json = input.bufferedReader().use { it.readText() }
        val obj = JSONObject(json)
        val array = obj.getJSONArray(categoria)

        val preguntas = mutableListOf<String>()
        for (i in 0 until array.length()) {
            preguntas.add(array.getString(i))
        }
        return preguntas
    }

    private fun guardarRespuestas(preguntas: List<String>, respuestas: Map<Int, String>) {
        val uid = auth.currentUser?.uid ?: return
        val fecha = fechaActual


        val respuestasFormateadas = preguntas.mapIndexed { index, pregunta ->
            pregunta to (respuestas[index] ?: "")
        }
        // 1. Guardar en SQLite
        val dbHelper = AppDatabaseHelper(this)
        dbHelper.guardarRespuestas(fecha, categoria, respuestasFormateadas)

        // 2. (opcional) Guardar en Firestore
        val firestoreDatos = respuestasFormateadas.map { (pregunta, respuesta) ->
            mapOf(
                "pregunta" to pregunta,
                "respuesta" to respuesta,
                "timestamp" to Timestamp.now()
            )
        }
        db.collection("respuestas")
            .document(uid)
            .collection("dias")
            .document(fecha)
            .set(mapOf("categoria" to categoria, "respuestas" to firestoreDatos))
            .addOnSuccessListener {
                Toast.makeText(this, "Respuestas guardadas", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}