package com.example.parkme.data.model

data class Reservation(
    val id: Int,
    val userId: String,
    val userName: String = "",
    val parkingId: Int,
    val parkingName: String,
    val date: String,
    val hour: String = "",
    val hours: Int,
    val totalPrice: Double,
    val status: String = "Confirmada",
    val plate: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id"          to id,
        "userId"      to userId,
        "userName"    to userName,
        "parkingId"   to parkingId,
        "parkingName" to parkingName,
        "date"        to date,
        "hour"        to hour,
        "hours"       to hours,
        "totalPrice"  to totalPrice,
        "status"      to status,
        "plate"       to plate
    )
}