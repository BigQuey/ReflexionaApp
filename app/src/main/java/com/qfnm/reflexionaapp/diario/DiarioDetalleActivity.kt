package com.qfnm.reflexionaapp.diario

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper

class DiarioDetalleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diario_detalle)

        val fecha = intent.getStringExtra("fecha") ?: return
        val texto = AppDatabaseHelper(this).obtenerDiario(fecha)

        findViewById<TextView>(R.id.tvDetalleFecha).text = "📅 $fecha"
        findViewById<TextView>(R.id.tvDetalleTexto).text = texto
    }
}