package com.example.parkme.data.model

data class Parking(

    val id: Int,
    val name: String,
    val address: String,
    val pricePerHour: Double,

    val ownerEmail: String,

    var totalSpaces: Int,
    var occupiedSpaces: Int,

    //  Coordenadas para el mapa
    val lat: Double = 0.0,
    val lng: Double = 0.0
)