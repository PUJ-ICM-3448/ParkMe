package com.example.parkme.location

import com.example.parkme.data.model.UserLocation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object LocationRepository {

    private val db = FirebaseDatabase.getInstance()
    private val activeUsersRef = db.getReference("activeUsers")

    fun sanitizeEmail(email: String): String =
        email.replace(".", "_").replace("@", "_at_")

    // ── Publicar mi ubicación ─────────────────────────────────────────────────

    fun publishMyLocation(email: String, name: String, latLng: LatLng) {
        val key = sanitizeEmail(email)
        val location = UserLocation(
            email       = email,
            name        = name,
            latitude    = latLng.latitude,
            longitude   = latLng.longitude,
            lastUpdated = System.currentTimeMillis(),
            isActive    = true
        )
        activeUsersRef.child(key).setValue(location.toMap())

        // ── CLAVE: onDisconnect garantiza que Firebase marque isActive=false
        // automáticamente cuando el dispositivo pierde conexión, cierra la app
        // de manera forzada, o se queda sin internet — SIN necesitar llamar logout().
        activeUsersRef.child(key).onDisconnect().updateChildren(
            mapOf(
                "isActive"    to false,
                "lastUpdated" to System.currentTimeMillis()
            )
        )
    }

    // ── Marcar inactivo (logout normal) ───────────────────────────────────────

    fun markInactive(email: String) {
        val key = sanitizeEmail(email)
        activeUsersRef.child(key).updateChildren(
            mapOf(
                "isActive"    to false,
                "lastUpdated" to System.currentTimeMillis()
            )
        )
    }

    // ── Eliminar presencia (logout definitivo) ────────────────────────────────

    fun removeMyPresence(email: String) {
        val key = sanitizeEmail(email)
        // Cancelar el onDisconnect antes de eliminar para no crear
        // una escritura fantasma después del removeValue
        activeUsersRef.child(key).onDisconnect().cancel()
        activeUsersRef.child(key).removeValue()
    }

    // ── Observar usuarios activos en tiempo real ──────────────────────────────

    fun observeActiveUsers(myEmail: String): Flow<List<UserLocation>> = callbackFlow {

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<UserLocation>()
                val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000L

                for (child in snapshot.children) {
                    try {
                        val email   = child.child("email").getValue(String::class.java) ?: continue
                        val name    = child.child("name").getValue(String::class.java) ?: ""
                        val lat     = child.child("latitude").getValue(Double::class.java) ?: continue
                        val lng     = child.child("longitude").getValue(Double::class.java) ?: continue
                        val updated = child.child("lastUpdated").getValue(Long::class.java) ?: 0L
                        val active  = child.child("isActive").getValue(Boolean::class.java) ?: false

                        if (email == myEmail) continue
                        if (!active) continue
                        if (updated < fiveMinutesAgo) continue

                        users.add(
                            UserLocation(
                                email       = email,
                                name        = name,
                                latitude    = lat,
                                longitude   = lng,
                                lastUpdated = updated,
                                isActive    = active
                            )
                        )
                    } catch (e: Exception) { /* ignorar nodos mal formateados */ }
                }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }

        activeUsersRef.addValueEventListener(listener)
        awaitClose { activeUsersRef.removeEventListener(listener) }
    }
}