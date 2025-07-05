package com.qfnm.reflexionaapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper
import com.qfnm.reflexionaapp.diario.DiarioListaActivity
import com.qfnm.reflexionaapp.estadisticas.EstadisticasActivity

class CuentaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuenta)

        auth = FirebaseAuth.getInstance()

        // Configurar GoogleSignInClient
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val user = auth.currentUser
        findViewById<TextView>(R.id.tvNombre).text = "👤 ${user?.displayName ?: "Sin nombre"}"
        findViewById<TextView>(R.id.tvCorreo).text = "📧 ${user?.email ?: "Sin correo"}"

        findViewById<Button>(R.id.btnVerDiario).setOnClickListener {
            startActivity(Intent(this, DiarioListaActivity::class.java))
        }

        findViewById<Button>(R.id.btnVerEstadisticas).setOnClickListener {
            startActivity(Intent(this, EstadisticasActivity::class.java))
        }

        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        val dbHelper = AppDatabaseHelper(this)
        dbHelper.borrarTodo()
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            // Borrar datos locales
            getSharedPreferences("config", MODE_PRIVATE).edit().clear().apply()

            // Ir al login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}