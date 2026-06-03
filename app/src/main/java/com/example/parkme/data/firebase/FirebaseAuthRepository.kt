package com.example.parkme.data.firebase

import com.example.parkme.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de autenticación usando Firebase Authentication.
 *
 * Estructura en Realtime Database:
 *   users/
 *     {uid}/
 *       name, email, plate, role, profilePhotoPath
 */
object FirebaseAuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseDatabase.getInstance()
    private val usersRef = db.getReference("users")

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Inicia sesión con email y contraseña.
     * Devuelve el User con sus datos de perfil (nombre, rol, placa) o null si falla.
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid    = result.user?.uid ?: return Result.failure(Exception("UID nulo"))

            // Leer perfil del usuario desde Realtime Database
            val snapshot = usersRef.child(uid).get().await()

            val name    = snapshot.child("name").getValue(String::class.java) ?: ""
            val role    = snapshot.child("role").getValue(String::class.java) ?: "CLIENT"
            val plate   = snapshot.child("plate").getValue(String::class.java) ?: ""
            val photo   = snapshot.child("profilePhotoPath").getValue(String::class.java)

            Result.success(User(name = name, email = email, password = "", plate = plate, role = role, profilePhotoPath = photo))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Registro ──────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo usuario en Firebase Auth y guarda su perfil en la DB.
     *
     * IMPORTANTE: Firebase hace signIn automático al crear la cuenta con
     * createUserWithEmailAndPassword. Para evitar que esto sobreescriba la
     * sesión activa del dueño (u otro usuario), aquí se cierra sesión
     * inmediatamente después de guardar los datos en la DB.
     * La app navegará a login para que el nuevo usuario ingrese explícitamente.
     */
    suspend fun register(name: String, email: String, password: String, plate: String, role: String): Result<User> {
        return try {
            // Guardar el uid del usuario actualmente logueado (si existe)
            val previousUid = auth.currentUser?.uid

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid    = result.user?.uid ?: return Result.failure(Exception("UID nulo"))

            // Guardar perfil en Realtime Database
            val profileData = mapOf(
                "name"  to name,
                "email" to email,
                "plate" to plate,
                "role"  to role,
                "profilePhotoPath" to ""
            )
            usersRef.child(uid).setValue(profileData).await()

            // Si había una sesión previa activa antes del registro, cerrar la nueva
            // sesión para no desplazar al usuario que ya estaba autenticado.
            // (Firebase no permite multi-sesión; createUserWithEmailAndPassword
            // hace signIn automático y desconecta la sesión anterior.)
            if (previousUid != null && previousUid != uid) {
                // Había otra sesión → cerrar la recién creada; la sesión anterior
                // ya fue invalidada por Firebase, así que hacemos signOut y
                // retornamos el usuario creado para que la UI redirija a login.
                auth.signOut()
            }

            Result.success(User(name = name, email = email, password = "", plate = plate, role = role))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() {
        auth.signOut()
    }

    // ── Estado actual ─────────────────────────────────────────────────────────

    /** Devuelve true si hay un usuario autenticado en Firebase */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    /** UID del usuario actualmente autenticado */
    fun currentUid(): String? = auth.currentUser?.uid

    /** Email del usuario actualmente autenticado */
    fun currentEmail(): String? = auth.currentUser?.email

    // ── Actualizar perfil ─────────────────────────────────────────────────────

    suspend fun updateProfile(uid: String, name: String, plate: String, photoPath: String?): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("name" to name, "plate" to plate)
            if (photoPath != null) updates["profilePhotoPath"] = photoPath
            usersRef.child(uid).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el perfil completo del usuario actualmente autenticado en Firebase.
     * Útil para restaurar la sesión en MockAuth cuando la app se reinicia en frío.
     */
    suspend fun fetchCurrentUserProfile(): Result<User> {
        return try {
            val firebaseUser = auth.currentUser
                ?: return Result.failure(Exception("No hay sesión activa"))
            val uid      = firebaseUser.uid
            val email    = firebaseUser.email ?: ""
            val snapshot = usersRef.child(uid).get().await()

            val name  = snapshot.child("name").getValue(String::class.java) ?: ""
            val role  = snapshot.child("role").getValue(String::class.java) ?: "CLIENT"
            val plate = snapshot.child("plate").getValue(String::class.java) ?: ""
            val photo = snapshot.child("profilePhotoPath").getValue(String::class.java)

            Result.success(User(name = name, email = email, password = "", plate = plate, role = role, profilePhotoPath = photo))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
