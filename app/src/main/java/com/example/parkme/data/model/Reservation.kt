package com.example.parkme.data.model

data class Reservation(
    val id: Int,
    val parkingId: Int,
    val userName: String,
    val plate: String,
    val hour: String
)