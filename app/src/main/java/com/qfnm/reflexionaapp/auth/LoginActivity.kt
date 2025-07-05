package com.qfnm.reflexionaapp.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.qfnm.reflexionaapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.qfnm.reflexionaapp.MainActivity
import com.qfnm.reflexionaapp.utils.sincronizarFirestoreASQLite


class LoginActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // viene de google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<SignInButton>(R.id.btnGoogleSignIn).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { authTask ->
                        if (authTask.isSuccessful) {
                            val usuario = authTask.result?.user
                            if (usuario != null) {
                                guardarUsuarioEnFirestore(usuario)
                            }

                            // Ir a pantalla principal
                            val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
                            val yaSincronizado = prefs.getBoolean("sincronizacionHecha", false)

                            if (!yaSincronizado) {
                                sincronizarFirestoreASQLite(this) {
                                    Toast.makeText(this, "Datos sincronizados desde la nube", Toast.LENGTH_SHORT).show()
                                }
                            }
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                        }
                    }

            } catch (e: ApiException) {
                Log.w("Login", "Fallo Google sign in", e)
            }
        }
    }
    fun guardarUsuarioEnFirestore(usuario: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()

        val datosUsuario = hashMapOf(
            "uid" to usuario.uid,
            "nombre" to (usuario.displayName ?: ""),
            "correo" to (usuario.email ?: ""),
            "fotoUrl" to (usuario.photoUrl?.toString() ?: ""),
            "fechaRegistro" to Timestamp.now()
        )

        db.collection("usuarios").document(usuario.uid)
            .set(datosUsuario, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Usuario guardado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar usuario", e)
            }
    }
}