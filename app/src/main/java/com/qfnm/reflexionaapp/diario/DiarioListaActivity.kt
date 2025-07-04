package com.qfnm.reflexionaapp.diario

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper

class DiarioListaActivity : AppCompatActivity() {
    private lateinit var dbHelper: AppDatabaseHelper
    private lateinit var listaFechas: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diario_lista)

        dbHelper = AppDatabaseHelper(this)
        val listaView = findViewById<ListView>(R.id.lvFechas)

        listaFechas = dbHelper.obtenerFechasDeDiario()

//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaFechas)
//        listaView.adapter = adapter
        val adapter = NotaAdapter(this, listaFechas, dbHelper)
        listaView.adapter = adapter

        listaView.setOnItemClickListener { _, _, pos, _ ->
            val fecha = listaFechas[pos]
            val intent = Intent(this, DiarioDetalleActivity::class.java)
            intent.putExtra("fecha", fecha)
            startActivity(intent)
        }
        listaView.setOnItemLongClickListener { _, _, pos, _ ->
            val fecha = listaFechas[pos]

            AlertDialog.Builder(this)
                .setTitle("¿Eliminar nota?")
                .setMessage("¿Estás seguro de que quieres eliminar la nota del $fecha?")
                .setPositiveButton("Sí") { _, _ ->
                    dbHelper.eliminarNotaPorFecha(this, fecha)
                    Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show()
                    actualizarLista()
                }
                .setNegativeButton("Cancelar", null)
                .show()

            true
        }
        findViewById<Button>(R.id.btnAgregarNota).setOnClickListener {
            startActivity(Intent(this, DiarioActivity::class.java))
        }

    }
    private fun actualizarLista() {
        listaFechas = dbHelper.obtenerFechasDeDiario()
        val adapter = NotaAdapter(this, listaFechas, dbHelper)
        findViewById<ListView>(R.id.lvFechas).adapter = adapter
    }

}