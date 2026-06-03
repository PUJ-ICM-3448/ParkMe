package com.example.parkme.data.mock

import com.example.parkme.data.model.User
import com.example.parkme.location.LocationRepository
import com.example.parkme.data.firebase.FirebaseAuthRepository

/**
 * MockAuth ahora actúa como puente entre Firebase Authentication y el resto
 * de la app. El login/registro real ocurre en FirebaseAuthRepository;
 * MockAuth solo mantiene en memoria quién es el usuario activo para que
 * todas las pantallas puedan consultarlo con MockAuth.currentUser.
 */
object MockAuth {

    var currentUser: User? = null
        private set

    /** Llamado por LoginScreen y RegisterScreen tras un login/registro exitoso en Firebase */
    fun setCurrentUser(user: User) {
        currentUser = user
    }

    /**
     * Cierra sesión: limpia presencia en Firebase Realtime Database,
     * cierra sesión en Firebase Auth y borra el usuario local.
     */
    fun logout() {
        currentUser?.let { user ->
            LocationRepository.removeMyPresence(user.email)
        }
        FirebaseAuthRepository.logout()
        currentUser = null
    }

    /** Actualiza los datos del usuario en memoria (tras editar perfil) */
    fun updateUser(updatedUser: User) {
        currentUser = updatedUser
    }

    /** Compatibilidad: devuelve true si hay sesión activa */
    fun isLoggedIn(): Boolean = currentUser != null || FirebaseAuthRepository.isLoggedIn()
}