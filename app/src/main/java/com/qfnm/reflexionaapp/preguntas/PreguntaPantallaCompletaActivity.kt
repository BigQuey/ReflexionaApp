package com.qfnm.reflexionaapp.preguntas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper
import com.qfnm.reflexionaapp.modelo.Pregunta
import com.qfnm.reflexionaapp.modelo.Respuesta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreguntaPantallaCompletaActivity : AppCompatActivity() {
    private lateinit var preguntas: List<Pregunta>
    private lateinit var respuestas: MutableList<String>
    private var index = 0

    private lateinit var ivIcono: TextView
    private lateinit var tvCategoria: TextView
    private lateinit var tvPregunta: TextView
    private lateinit var etRespuesta: EditText
    private lateinit var btnSiguiente: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val fecha: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregunta_pantalla_completa)

        preguntas = intent.getSerializableExtra("preguntas") as? List<Pregunta> ?: emptyList()
        respuestas = MutableList(preguntas.size) { "" }

        ivIcono = findViewById(R.id.ivIconoCategoria)
        tvCategoria = findViewById(R.id.tvCategoria)
        tvPregunta = findViewById(R.id.tvPregunta)
        etRespuesta = findViewById(R.id.etRespuesta)
        btnSiguiente = findViewById(R.id.btnSiguiente)

        mostrarPregunta()

        btnSiguiente.setOnClickListener {
            val respuesta = etRespuesta.text.toString().trim()
            if (respuesta.isBlank()) {
                Toast.makeText(this, "Por favor escribe tu respuesta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            respuestas[index] = respuesta
            index++

            if (index < preguntas.size) {
                mostrarPregunta()
            } else {
                guardarRespuestas()
                Toast.makeText(this, "Respuestas guardadas", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, com.qfnm.reflexionaapp.MainActivity::class.java))
                finish()
            }
        }
    }

    private fun mostrarPregunta() {
        val pregunta = preguntas[index]
        tvCategoria.text = pregunta.categoria
        tvPregunta.text = pregunta.texto
        etRespuesta.setText("")

        // Cambia el ícono según la categoría
        val icono = when (pregunta.categoria.lowercase()) {
            "emociones" -> "💗"
            "autoestima" -> "🤩"
            "propósitos" -> "🏆"
            "relaciones" -> "👥"
            else -> "🧠"
        }
        ivIcono.setText(icono)
    }

    private fun guardarRespuestas() {
        val dbHelper = AppDatabaseHelper(this)

        val lista = preguntas.mapIndexed { i, pregunta ->
            Respuesta(
                pregunta = pregunta.texto,
                respuesta = respuestas[i],
                fecha = fecha,
                categoria = pregunta.categoria
            )
        }

        // Guardar en SQLite
        dbHelper.guardarRespuestas(fecha, "", lista.map { it.pregunta to it.respuesta })

        // Guardar en Firestore
        val uid = auth.currentUser?.uid ?: return
        val datosFirestore = mapOf(
            "fecha" to fecha,
            "respuestas" to lista.map {
                mapOf(
                    "categoria" to it.categoria,
                    "pregunta" to it.pregunta,
                    "respuesta" to it.respuesta,
                    "timestamp" to Timestamp.now()
                )
            }
        )

        firestore.collection("respuestas")
            .document(uid)
            .collection("dias")
            .document(fecha)
            .set(datosFirestore)
    }
}