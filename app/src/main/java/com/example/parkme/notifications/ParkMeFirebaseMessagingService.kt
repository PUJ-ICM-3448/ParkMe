package com.example.parkme.notifications

import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.firebase.FirebaseAuthRepository
import com.example.parkme.location.LocationRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ParkMeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "ParkMe"
        val body  = remoteMessage.notification?.body  ?: remoteMessage.data["body"]  ?: ""
        val type  = remoteMessage.data["type"] ?: "general"

        when (type) {
            "chat" -> ParkMeNotificationHelper.showChatNotification(
                context     = applicationContext,
                senderName  = remoteMessage.data["sender"] ?: "Usuario",
                messageText = body,
                parkingName = remoteMessage.data["parking"] ?: "Parqueadero"
            )
            "reservation" -> ParkMeNotificationHelper.showReservationNotification(
                context     = applicationContext,
                parkingName = remoteMessage.data["parking"] ?: "Parqueadero",
                date        = remoteMessage.data["date"] ?: "",
                hours       = remoteMessage.data["hours"]?.toIntOrNull() ?: 1
            )
            else -> ParkMeNotificationHelper.showChatNotification(
                context     = applicationContext,
                senderName  = "ParkMe",
                messageText = body,
                parkingName = title
            )
        }
    }

    override fun onNewToken(token: String) {
        // Intentar guardar el token con el usuario en MockAuth primero.
        // Si MockAuth no tiene usuario (arranque en frío), consultar Firebase Auth
        // directamente para obtener el uid/email del usuario activo.
        val email = MockAuth.currentUser?.email
            ?: FirebaseAuthRepository.currentEmail()

        if (email != null) {
            saveToken(token, email)
        } else {
            // Último recurso: recuperar el perfil desde Firebase DB
            CoroutineScope(Dispatchers.IO).launch {
                val result = FirebaseAuthRepository.fetchCurrentUserProfile()
                result.onSuccess { user ->
                    saveToken(token, user.email)
                    // Sincronizar MockAuth si estaba vacío
                    if (MockAuth.currentUser == null) {
                        MockAuth.setCurrentUser(user)
                    }
                }
            }
        }
    }

    private fun saveToken(token: String, email: String) {
        val key = LocationRepository.sanitizeEmail(email)
        FirebaseDatabase.getInstance()
            .getReference("fcmTokens")
            .child(key)
            .setValue(mapOf("token" to token, "email" to email))
    }
}
