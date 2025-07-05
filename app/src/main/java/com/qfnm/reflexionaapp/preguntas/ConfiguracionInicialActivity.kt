package com.qfnm.reflexionaapp.preguntas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qfnm.reflexionaapp.MainActivity
import com.qfnm.reflexionaapp.R

class ConfiguracionInicialActivity : AppCompatActivity() {
    private lateinit var etCantidad: EditText
    private lateinit var btnGuardar: Button
    private lateinit var listaCategorias: ListView

    private val categorias = listOf("Emociones", "Autoestima", "Propósitos", "Relaciones")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion_inicial)

        etCantidad = findViewById(R.id.etCantidad)
        btnGuardar = findViewById(R.id.btnGuardar)
        listaCategorias = findViewById(R.id.listaCategorias)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, categorias)
        listaCategorias.adapter = adapter
        listaCategorias.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val cantidadGuardada = prefs.getInt("cantidadPreguntas", 3)
        val categoriasGuardadas = prefs.getStringSet("categoriasElegidas", emptySet())
        etCantidad.setText(cantidadGuardada.toString())

        for (i in categorias.indices) {
            if (categoriasGuardadas?.contains(categorias[i]) == true) {
                listaCategorias.setItemChecked(i, true)
            }
        }

        btnGuardar.setOnClickListener {
            val cantidad = etCantidad.text.toString().toIntOrNull()
            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val seleccionadas = mutableSetOf<String>()
            for (i in categorias.indices) {
                if (listaCategorias.isItemChecked(i)) {
                    seleccionadas.add(categorias[i])
                }
            }

            if (seleccionadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("cantidadPreguntas", cantidad)
                .putStringSet("categoriasElegidas", seleccionadas)
                .putBoolean("configHecha", true)
                .apply()
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()

            //SIN REUTILIZACION
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()


            //PARA REUTILIZARLO
            if (!isTaskRoot) {
                finish() // Solo cerramos la pantalla si se accedió desde menú
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}