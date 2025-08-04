package com.example.deflatam_pruebafinal.utilidades

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.deflatam_pruebafinal.MainActivity // Asegúrate que esta sea tu actividad principal
import com.example.deflatam_pruebafinal.R // Asumiendo que tienes un recurso de icono, sino se puede omitir o usar uno de sistema

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "paseo_reminder_channel"
        // const val NOTIFICATION_ID = 1 
        const val EXTRA_PASEO_ID = "extra_paseo_id"
        const val EXTRA_MASCOTA_NOMBRE = "extra_mascota_nombre"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val mascotaNombre = intent.getStringExtra(EXTRA_MASCOTA_NOMBRE) ?: "Tu mascota"
        val paseoId = intent.getLongExtra(EXTRA_PASEO_ID, -1L)

        // utiliza NotificationCompat.Builder para construir la notificación
        val notificationIdForManager = if (paseoId != -1L && paseoId != 0L) paseoId.toInt() else System.currentTimeMillis().toInt()

        // Crea el canal de notificación si es necesario
        createNotificationChannel(context)

        // Crea el intent para abrir la actividad principal
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Agrega el ID del paseo al intent
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construye la notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Reemplaza con tu ícono.
            .setContentTitle("¡Falta poco para el paseo!") // Title as requested
            .setContentText("Estamos recordando que te falta poco para el paseo de $mascotaNombre.") // Text as requested (customized)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Muestra la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationIdForManager, builder.build())
    }

    /** Crea el canal de notificación*/
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Paseos"
            val descriptionText = "Canal para notificaciones de recordatorios de paseos."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
