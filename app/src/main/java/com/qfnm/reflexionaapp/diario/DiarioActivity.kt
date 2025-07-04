package com.qfnm.reflexionaapp.diario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qfnm.reflexionaapp.MainActivity
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiarioActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dbHelper: AppDatabaseHelper

    private lateinit var etDiario: EditText
    private lateinit var tvFecha: TextView
    private lateinit var btnGuardar: Button

    private val fechaActual: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diario)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        dbHelper = AppDatabaseHelper(this)

        etDiario = findViewById(R.id.etDiario)
        tvFecha = findViewById(R.id.tvFecha)
        btnGuardar = findViewById(R.id.btnGuardar)

        tvFecha.text = "📅 ${fechaActual}"
        //cargar desde SQLite
        val textoLocal = dbHelper.obtenerDiario(fechaActual)
        if (textoLocal != null) {
            etDiario.setText(textoLocal)
        } else {
            // Si no hay en local, intentamos desde Firestore
            val uid = auth.currentUser?.uid
            if (uid != null) {
                db.collection("diarios")
                    .document(uid)
                    .collection("entradas")
                    .document(fechaActual)
                    .get()
                    .addOnSuccessListener { doc ->
                        val texto = doc.getString("texto")
                        if (!texto.isNullOrBlank()) {
                            etDiario.setText(texto)
                            // Y lo guardamos localmente
                            dbHelper.guardarDiario(fechaActual, texto)
                        }
                    }
            }
        }


        val uid = auth.currentUser?.uid
        if (uid != null) {
            cargarDiario(uid)
        }

        btnGuardar.setOnClickListener {
            intent = Intent(this,MainActivity::class.java)

            val texto = etDiario.text.toString()
            if (texto.isNotBlank()) {
                guardarDiarioEnAmbos(texto)
                startActivity(intent)
            } else {
                Toast.makeText(this, "El diario no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarDiario(uid: String) {
        db.collection("diarios")
            .document(uid)
            .collection("entradas")
            .document(fechaActual)
            .get()
            .addOnSuccessListener { doc ->
                val texto = doc.getString("texto")
                if (!texto.isNullOrBlank()) {
                    etDiario.setText(texto)
                }
            }
    }


    private fun guardarDiarioEnAmbos(texto: String) {
        val uid = auth.currentUser?.uid ?: return

        // Guardar en SQLite local
        dbHelper.guardarDiario(fechaActual, texto)

        // Guardar en Firestore
        val datos = mapOf(
            "texto" to texto,
            "fechaGuardado" to Timestamp.now()
        )

        db.collection("diarios")
            .document(uid)
            .collection("entradas")
            .document(fechaActual)
            .set(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Guardado local y en la nube", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Guardado local, pero falló en la nube", Toast.LENGTH_SHORT).show()
            }
    }
}