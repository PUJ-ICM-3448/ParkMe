package com.example.parkme.data.mock

import com.example.parkme.data.model.Reservation

object MockReservationData {

    private val reservations = mutableListOf<Reservation>()

    fun addReservation(reservation: Reservation) {
        reservations.add(reservation)
    }

    fun getReservationsByParking(parkingId: Int): List<Reservation> {
        return reservations.filter { it.parkingId == parkingId }
    }

}