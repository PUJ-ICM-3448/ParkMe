package com.example.parkme.data.model

data class Message(
    val id: Int = 0,
    val sender: String = "",
    val senderId: String = "",   // email del remitente (para identificar "soy yo")
    val text: String = "",
    val timestamp: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "sender"    to sender,
        "senderId"  to senderId,
        "text"      to text,
        "timestamp" to timestamp
    )
}
