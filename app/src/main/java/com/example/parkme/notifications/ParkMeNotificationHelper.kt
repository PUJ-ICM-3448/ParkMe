package com.example.parkme.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.parkme.MainActivity
import com.example.parkme.R

/**
 * Helper centralizado para todas las notificaciones locales de ParkMe.
 *
 * Canales:
 *  - CHAT_CHANNEL     → mensajes de chat
 *  - RESERVE_CHANNEL  → confirmaciones de reserva
 *  - TRACKING_CHANNEL → alertas de usuarios cercanos
 */
object ParkMeNotificationHelper {

    private const val CHAT_CHANNEL     = "parkme_chat"
    private const val RESERVE_CHANNEL  = "parkme_reservas"
    private const val TRACKING_CHANNEL = "parkme_tracking_alerts"

    /** Debe llamarse al iniciar la app (en MainActivity.onCreate) */
    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val chatChannel = NotificationChannel(
            CHAT_CHANNEL, "Mensajes de Chat", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Notificaciones de nuevos mensajes en el chat del parqueadero" }

        val reserveChannel = NotificationChannel(
            RESERVE_CHANNEL, "Reservas", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Confirmaciones y recordatorios de reservas" }

        val trackingChannel = NotificationChannel(
            TRACKING_CHANNEL, "Usuarios Cercanos", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Alertas sobre usuarios activos en tu zona" }

        manager.createNotificationChannel(chatChannel)
        manager.createNotificationChannel(reserveChannel)
        manager.createNotificationChannel(trackingChannel)
    }

    /** Notificación de nuevo mensaje en chat */
    fun showChatNotification(
        context: Context,
        senderName: String,
        messageText: String,
        parkingName: String
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHAT_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nuevo mensaje — $parkingName")
            .setContentText("$senderName: $messageText")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$senderName: $messageText"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    /** Notificación de reserva confirmada */
    fun showReservationNotification(
        context: Context,
        parkingName: String,
        date: String,
        hours: Int
    ) {
        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, RESERVE_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✅ Reserva confirmada")
            .setContentText("$parkingName · $date · $hours hora(s)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tu reserva en $parkingName para el $date por $hours hora(s) está confirmada."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    /** Notificación cuando hay usuarios activos cerca */
    fun showUsersNearbyNotification(context: Context, count: Int) {
        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, TRACKING_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(" Usuarios activos cerca")
            .setContentText("$count usuario(s) están usando ParkMe en tu zona ahora.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}
