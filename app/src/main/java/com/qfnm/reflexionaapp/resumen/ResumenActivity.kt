package com.qfnm.reflexionaapp.resumen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.api.ReflexionApi
import com.qfnm.reflexionaapp.api.ResumenRequest
import com.qfnm.reflexionaapp.api.ResumenResponse
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper
import com.qfnm.reflexionaapp.modelo.RespuestaApi
import com.qfnm.reflexionaapp.utils.formatearRespuestasComoJson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ResumenActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tvResumen: TextView
    private lateinit var btnCompartir: Button
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen)
        val dbHelper = AppDatabaseHelper(this)
        val respuestasPorDia = dbHelper.obtenerRespuestasUltimos7Dias()
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        tvResumen = findViewById(R.id.tvResumen)
        progressBar = findViewById(R.id.progressBar)
        findViewById<Button>(R.id.btnAnalizar).setOnClickListener {
            obtenerRespuestasYEnviar()
        }
        respuestasPorDia.forEach { 
            Log.d("SQLite", "Fecha: ${it.fecha}, Pregunta: ${it.pregunta}, Respuesta: ${it.respuesta}")
        }
        btnCompartir = findViewById(R.id.btnCompartir)

        btnCompartir.setOnClickListener {
            val texto = tvResumen.text.toString()

            if (texto.isBlank() || texto.contains("aquí aparecerá", ignoreCase = true)) {
                Toast.makeText(this, "Aún no hay resumen para compartir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Mi Resumen Reflexivo")
                putExtra(Intent.EXTRA_TEXT, texto)
            }

            startActivity(Intent.createChooser(intent, "Compartir con..."))
        }
    }

    private fun obtenerRespuestasYEnviar() {
        val uid = auth.currentUser?.uid ?: return
        val diasRef = db.collection("respuestas").document(uid).collection("dias")
        tvResumen.text = "Analizando tus respuestas...\nEsto puede tardar unos segundos ☁️"
        progressBar.visibility = View.VISIBLE
        val hace7Dias = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time

        diasRef.get().addOnSuccessListener { snapshots ->
            val respuestas = mutableListOf<RespuestaApi>()

            for (doc in snapshots.documents) {
                val fecha = doc.id
                val data = doc.get("respuestas") as? List<Map<String, Any>> ?: continue
                data.forEach {
                    respuestas.add(
                        RespuestaApi(
                            fecha = fecha,
                            pregunta = it["pregunta"].toString(),
                            respuesta = it["respuesta"].toString()
                        )
                    )
                }
            }

            enviarAApi(respuestas)
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            tvResumen.text = "Error al obtener respuestas."
        }

        //IMPLEMENTAR LUEGO
        //TODO
        /*
        val dbHelper = AppDatabaseHelper(this)
        val respuestas = dbHelper.obtenerRespuestasUltimos7Dias()

        val json = formatearRespuestasComoJson(respuestas)
        Log.d("JSON", json)

         */
    }

    private fun enviarAApi(respuestas: List<RespuestaApi>) {
        val client = OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()

        val api = Retrofit.Builder()
            .baseUrl("https://api-reflexionaapp.onrender.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReflexionApi::class.java)

        val request = ResumenRequest(respuestas)

        api.enviarResumen(request).enqueue(object : Callback<ResumenResponse> {
            override fun onResponse(call: Call<ResumenResponse>, response: Response<ResumenResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    tvResumen.text = response.body()?.mensaje ?: "Resumen recibido sin mensaje."
                } else {
                    tvResumen.text = "Error en el análisis: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<ResumenResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvResumen.text = "Error al conectar con la API: ${t.message}"
            }
        })
    }
}