package com.example.parkme.data.model

/**
 * Modelo que representa la ubicación en tiempo real de un usuario activo.
 * Se almacena y sincroniza en Firebase Realtime Database bajo:
 *   /activeUsers/{email_sanitizado}/
 */
data class UserLocation(
    val email: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastUpdated: Long = 0L,
    val isActive: Boolean = true
) {
    /**
     * Convierte a Map para enviar a Firebase
     * (Firebase requiere Map o POJO con constructor vacío)
     */
    fun toMap(): Map<String, Any> = mapOf(
        "email"       to email,
        "name"        to name,
        "latitude"    to latitude,
        "longitude"   to longitude,
        "lastUpdated" to lastUpdated,
        "isActive"    to isActive
    )
}