package com.example.parkme.data.model

data class AppNotification(
    val id: Int,
    val text: String,
    val time: String,
    val userEmail: String,
    val ownerEmail: String
)