package com.qfnm.reflexionaapp.diario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper

class DiarioListaActivity : AppCompatActivity() {
    private lateinit var dbHelper: AppDatabaseHelper
    private lateinit var listaFechas: List<String>
    private lateinit var progressBar: ProgressBar
    private lateinit var tvVacio: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diario_lista)

        dbHelper = AppDatabaseHelper(this)
        val listaView = findViewById<ListView>(R.id.lvFechas)

        listaFechas = dbHelper.obtenerFechasDeDiario()

//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaFechas)
//        listaView.adapter = adapter
        tvVacio = findViewById(R.id.tvVacio)
        progressBar = findViewById(R.id.progressBar)
        sincronizarDesdeFirestore()
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
        findViewById<Button>(R.id.btnBuscarNota).setOnClickListener{
            startActivity(Intent(this,DiarioBuscarActivity::class.java))
        }

    }
    override fun onResume() {
        super.onResume()
        sincronizarDesdeFirestore()
    }
    private fun actualizarLista() {
        listaFechas = dbHelper.obtenerFechasDeDiario()
        if (listaFechas.isEmpty()) {
            tvVacio.visibility = View.VISIBLE
        } else {
            tvVacio.visibility = View.GONE
        }
        val adapter = NotaAdapter(this, listaFechas, dbHelper)
        findViewById<ListView>(R.id.lvFechas).adapter = adapter
    }

    private fun sincronizarDesdeFirestore() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        firestore.collection("diarios")
            .document(uid)
            .collection("entradas")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result.documents) {
                    val fecha = doc.id
                    val texto = doc.getString("texto") ?: continue
                    dbHelper.guardarDiario(fecha, texto)
                }

                progressBar.visibility = View.GONE
                actualizarLista()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error al sincronizar con la nube", Toast.LENGTH_SHORT).show()
            }
    }
}