package com.qfnm.reflexionaapp.diario

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper
import java.util.Calendar

class DiarioBuscarActivity : AppCompatActivity() {
    private lateinit var dbHelper: AppDatabaseHelper
    private lateinit var etTexto: EditText
    private lateinit var tvFecha: TextView
    private lateinit var btnFecha: Button

    private var fechaSeleccionada = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diario_buscar)

        dbHelper = AppDatabaseHelper(this)
        etTexto = findViewById(R.id.etTextoDiario)
        tvFecha = findViewById(R.id.tvFechaElegida)
        btnFecha = findViewById(R.id.btnElegirFecha)

        btnFecha.setOnClickListener {
            val hoy = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val mes = (month + 1).toString().padStart(2, '0')
                    val dia = day.toString().padStart(2, '0')
                    fechaSeleccionada = "$year-$mes-$dia"
                    tvFecha.text = "📅 $fechaSeleccionada"

                    val texto = dbHelper.obtenerDiario(fechaSeleccionada)
                    if (texto != null) {
                        etTexto.setText(texto)
                    } else {
                        etTexto.setText("No hay entrada para esta fecha.")
                    }
                },
                hoy.get(Calendar.YEAR),
                hoy.get(Calendar.MONTH),
                hoy.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}