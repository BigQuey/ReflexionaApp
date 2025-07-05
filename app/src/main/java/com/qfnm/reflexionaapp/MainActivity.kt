package com.qfnm.reflexionaapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.qfnm.reflexionaapp.auth.CuentaActivity
import com.qfnm.reflexionaapp.diario.DiarioActivity
import com.qfnm.reflexionaapp.diario.DiarioListaActivity
import com.qfnm.reflexionaapp.estadisticas.EstadisticasActivity
import com.qfnm.reflexionaapp.notificaciones.NotificacionesActivity
import com.qfnm.reflexionaapp.preguntas.ConfiguracionInicialActivity
import com.qfnm.reflexionaapp.preguntas.PreguntaActivity
import com.qfnm.reflexionaapp.preguntas.PreguntaPantallaCompletaActivity
import com.qfnm.reflexionaapp.preguntas.PreguntasConfigActivity
import com.qfnm.reflexionaapp.resumen.ResumenActivity
import com.qfnm.reflexionaapp.utils.PreguntaProvider

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var tvBienvenida: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("configHecha", false)) {
            startActivity(Intent(this, ConfiguracionInicialActivity::class.java))
            finish()
        }


        auth = FirebaseAuth.getInstance()

        //tvBienvenida = findViewById(R.id.tvBienvenida)
        val usuario = auth.currentUser
//        if (usuario != null) {
//            tvBienvenida.text = "Hola, ${usuario.displayName ?: "usuario"} 👋"
//        }

        val opDiario = findViewById<LinearLayout>(R.id.opDiario)
        val opHistorial = findViewById<LinearLayout>(R.id.opHistorial)
        val opResumen = findViewById<LinearLayout>(R.id.opResumen)
        val opRecordatorios = findViewById<LinearLayout>(R.id.opRecordatorios)

        val opEstadisticas = findViewById<LinearLayout>(R.id.opEstadisticas)
        val opResumenPreguntas = findViewById<LinearLayout>(R.id.opResumenPreguntas)
        val opConfiguracion = findViewById<LinearLayout>(R.id.opConfiguracion)
        val opCuenta = findViewById<LinearLayout>(R.id.opMenuCuenta)
        configurarOpcion(opDiario, "Mi Diario", "Exprésate libremente sobre tu día", R.drawable.baseline_menu_book_24)
        configurarOpcion(opHistorial, "Mi Historial", "Revisa tus días pasados", R.drawable.outline_calendar_month_24)
        configurarOpcion(opResumen, "Preguntas", "Comenzemos con las preguntas", R.drawable.outline_diversity_2_24)
        configurarOpcion(opRecordatorios, "Recordatorios", "Programa alertas para escribir tu diario", R.drawable.outline_av_timer_24)

        configurarOpcion(opEstadisticas, "Estadísticas", "Visualiza tu progreso", R.drawable.outline_bar_chart_24)
        configurarOpcion(opResumenPreguntas,"Crear Resumen","Ve el resumen de tu respuestas",R.drawable.outline_autorenew_24 )
        configurarOpcion(opConfiguracion, "Configuracion","Actualiza tu configuracion", R.drawable.outline_rule_settings_24)
        configurarOpcion(opCuenta,"Cuenta / Cerrar Sesión", "Observa tus datos o Cambia de cuenta", R.drawable.baseline_assignment_ind_24)
        //BOTON DE PREGUNTAS DIARIAS
        opResumen.setOnClickListener {
            val preguntas = PreguntaProvider.cargarPreguntas(this)

            if (preguntas.isEmpty()) {
                Toast.makeText(this, "No hay preguntas disponibles", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, PreguntaPantallaCompletaActivity::class.java)
                intent.putExtra("preguntas", ArrayList(preguntas)) // Serializable
                startActivity(intent)
            }
        }

        // BOTON DE NUEVO DIARIO
        opDiario.setOnClickListener {
            startActivity(Intent(this, DiarioActivity::class.java))
        }
        opHistorial.setOnClickListener {
            startActivity(Intent(this, DiarioListaActivity::class.java))
        }
        opRecordatorios.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }

        opEstadisticas.setOnClickListener {
            startActivity(Intent(this, EstadisticasActivity::class.java))
        }
        opResumenPreguntas.setOnClickListener{
            startActivity(Intent(this, ResumenActivity::class.java))
        }

        opConfiguracion.setOnClickListener {
            startActivity(Intent(this, ConfiguracionInicialActivity::class.java))
        }
        opCuenta.setOnClickListener{
            startActivity(Intent(this, CuentaActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100 // código de solicitud
                )
            }
        }

        crearCanalDeNotificacion()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se podrán mostrar notificaciones", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "reflexion_channel",
                "Notificaciones Diarias",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Te recuerda reflexionar diariamente"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }
    private fun configurarOpcion(
        layout: LinearLayout,
        titulo: String,
        descripcion: String,
        icono: Int
    ) {
        layout.findViewById<TextView>(R.id.tvTitulo).text = titulo
        layout.findViewById<TextView>(R.id.tvDescripcion).text = descripcion
        layout.findViewById<ImageView>(R.id.ivIcono).setImageResource(icono)
    }
}