package com.qfnm.reflexionaapp.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.qfnm.reflexionaapp.R

class NotificacionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notification = NotificationCompat.Builder(context, "reflexion_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("ReflexionaApp")
            .setContentText("¿Ya escribiste tu diario o respondiste tus preguntas?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // No hacer nada si no da el permiso
            return
        }
        notificationManager.notify(1001, notification)
    }

}