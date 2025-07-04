package com.qfnm.reflexionaapp.preguntas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qfnm.reflexionaapp.R

class PreguntasConfigActivity : AppCompatActivity() {
    private lateinit var spinnerCategoria: Spinner
    private lateinit var etCantidad: EditText
    private lateinit var btnComenzar: Button

    private val categorias = listOf("Emociones", "Autoestima", "Propósitos", "Relaciones")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preguntas_config)

        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        etCantidad = findViewById(R.id.etCantidad)
        btnComenzar = findViewById(R.id.btnComenzar)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter
        val intent = Intent(this, PreguntasResponderActivity::class.java)
        btnComenzar.setOnClickListener {
            val categoria = spinnerCategoria.selectedItem.toString()
            val cantidadTexto = etCantidad.text.toString()
            val cantidad = cantidadTexto.toIntOrNull()

            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            intent.putExtra("categoria", categoria)
            intent.putExtra("cantidad", cantidad)
            startActivity(intent)
        }
    }
}