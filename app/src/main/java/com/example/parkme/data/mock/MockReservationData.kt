package com.example.parkme.data.mock

import com.example.parkme.data.model.Reservation

object MockReservationData {

    private val reservations = mutableListOf<Reservation>()

    fun addReservation(reservation: Reservation) {
        reservations.add(reservation)
    }

    fun getReservationsForUser(email: String): List<Reservation> =
        reservations.filter { it.userId == email }

    fun getReservationsByParking(parkingId: Int): List<Reservation> =
        reservations.filter { it.parkingId == parkingId }

    fun getReservationById(id: Int): Reservation? =
        reservations.find { it.id == id }

    fun getAllReservations(): List<Reservation> = reservations.toList()
}