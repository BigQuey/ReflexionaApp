package com.qfnm.reflexionaapp.notificaciones

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.utils.cancelarNotificacionDiaria
import com.qfnm.reflexionaapp.utils.estaNotificacionProgramada
import com.qfnm.reflexionaapp.utils.programarNotificacionDiaria

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var tvHoraSeleccionada: TextView
    private lateinit var btnSeleccionarHora: Button
    private lateinit var btnGuardar: Button
    private lateinit var etMensaje: EditText
    private lateinit var switchRecordatorio: Switch
    private lateinit var selectorDias: LinearLayout

    private var diasSeleccionados = mutableSetOf<Int>()
    private var horaSeleccionada = 9
    private var minutoSeleccionado = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        tvHoraSeleccionada = findViewById(R.id.tvHoraSeleccionada)
        btnSeleccionarHora = findViewById(R.id.btnSeleccionarHora)
        btnGuardar = findViewById(R.id.btnGuardar)

        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        horaSeleccionada = prefs.getInt("horaNoti", 9)
        minutoSeleccionado = prefs.getInt("minutoNoti", 0)
        actualizarTexto()

        btnSeleccionarHora.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                horaSeleccionada = hourOfDay
                minutoSeleccionado = minute
                actualizarTexto()
            }, horaSeleccionada, minutoSeleccionado, true).show()
        }

//        val btnDesactivar = findViewById<Button>(R.id.btnDesactivar)
//        btnDesactivar.setOnClickListener {
//            cancelarNotificacionDiaria(this)
//            Toast.makeText(this, "Notificación diaria cancelada", Toast.LENGTH_SHORT).show()
//        }

        etMensaje = findViewById(R.id.etMensaje)
        switchRecordatorio = findViewById(R.id.switchRecordatorio)
        selectorDias = findViewById(R.id.selectorDias)

        val mensajeGuardado = prefs.getString("mensajeNoti", "No olvides escribir tu día ⭐")
        etMensaje.setText(mensajeGuardado)

        switchRecordatorio.isChecked = estaNotificacionProgramada(this)

        val diasGuardados = prefs.getString("diasNoti", "") ?: ""
        diasSeleccionados = diasGuardados.split(",").mapNotNull { it.toIntOrNull() }.toMutableSet()

        val dias = listOf("L", "M", "M", "J", "V", "S", "D")
        for ((index, dia) in dias.withIndex()) {
            val diaView = TextView(this).apply {
                text = dia
                textSize = 16f
                setPadding(24, 16, 24, 16)
                background = getDrawable(R.drawable.bg_dia_off)
                setTextColor(getColor(android.R.color.black))
                setOnClickListener {
                    val diaNumero = index + 1 // 1 = Lunes
                    if (diasSeleccionados.contains(diaNumero)) {
                        diasSeleccionados.remove(diaNumero)
                        background = getDrawable(R.drawable.bg_dia_off)
                    } else {
                        diasSeleccionados.add(diaNumero)
                        background = getDrawable(R.drawable.bg_dia_on)
                    }
                }
            }
            selectorDias.addView(diaView)
        }


        val estaProgramada = estaNotificacionProgramada(this)
        if (estaProgramada) {
            tvHoraSeleccionada.text = "✅ Notificación activa a las %02d:%02d".format(horaSeleccionada, minutoSeleccionado)
        } else {
            tvHoraSeleccionada.text = "🔕 Notificación no programada"
        }

//        btnGuardar.setOnClickListener {
//            prefs.edit()
//                .putInt("horaNoti", horaSeleccionada)
//                .putInt("minutoNoti", minutoSeleccionado)
//                .apply()
//
//            programarNotificacionDiaria(this, horaSeleccionada, minutoSeleccionado)
//            Toast.makeText(this, "Notificación diaria programada", Toast.LENGTH_SHORT).show()
//        }
        btnGuardar.setOnClickListener {
            val mensaje = etMensaje.text.toString()
            val activar = switchRecordatorio.isChecked

            prefs.edit()
                .putInt("horaNoti", horaSeleccionada)
                .putInt("minutoNoti", minutoSeleccionado)
                .putString("mensajeNoti", mensaje)
                .putBoolean("activarNoti", activar)
                .putString("diasNoti", diasSeleccionados.joinToString(","))
                .apply()

            if (activar) {
                programarNotificacionDiaria(this, horaSeleccionada, minutoSeleccionado)
                Toast.makeText(this, "Notificación programada", Toast.LENGTH_SHORT).show()
            } else {
                cancelarNotificacionDiaria(this)
                Toast.makeText(this, "Recordatorio desactivado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarTexto() {
        tvHoraSeleccionada.text = "Notificación a las %02d:%02d".format(horaSeleccionada, minutoSeleccionado)
    }
}