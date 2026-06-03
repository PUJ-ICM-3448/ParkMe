package com.example.parkme.data.model

data class User(

    val name: String,
    val email: String,
    val password: String,
    val plate: String,
    val role: String,                        // "CLIENT" o "OWNER"
    val profilePhotoPath: String? = null     // Ruta en almacenamiento interno

)