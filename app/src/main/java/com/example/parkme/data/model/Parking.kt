package com.example.parkme.data.model

data class Parking(

    val id: Int,
    val name: String,
    val address: String,
    val pricePerHour: Double,

    val ownerEmail: String,

    var totalSpots: Int,
    var availableSpots: Int

)