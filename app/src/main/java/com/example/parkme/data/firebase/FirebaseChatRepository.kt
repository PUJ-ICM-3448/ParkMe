package com.example.parkme.data.firebase

import com.example.parkme.data.model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repositorio de chat en tiempo real usando Firebase Realtime Database.
 *
 * Estructura en Firebase:
 *   chats/
 *     {parkingId}/
 *       {messageId}/
 *         sender, text, timestamp, senderId
 */
object FirebaseChatRepository {

    private val db       = FirebaseDatabase.getInstance()
    private val chatsRef = db.getReference("chats")

    /** Envía un mensaje al chat de un parqueadero */
    fun sendMessage(parkingId: Int, message: Message) {
        val key = chatsRef.child(parkingId.toString()).push().key ?: return
        chatsRef.child(parkingId.toString()).child(key).setValue(message.toMap())
    }

    /**
     * Observa los mensajes de un chat en tiempo real.
     * Emite la lista completa cada vez que llega un nuevo mensaje.
     */
    fun observeMessages(parkingId: Int): Flow<List<Message>> = callbackFlow {
        val ref = chatsRef.child(parkingId.toString())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (child in snapshot.children) {
                    try {
                        val sender    = child.child("sender").getValue(String::class.java) ?: ""
                        val text      = child.child("text").getValue(String::class.java) ?: ""
                        val timestamp = child.child("timestamp").getValue(String::class.java) ?: ""
                        val senderId  = child.child("senderId").getValue(String::class.java) ?: ""
                        messages.add(Message(
                            id        = child.key.hashCode(),
                            sender    = sender,
                            text      = text,
                            timestamp = timestamp,
                            senderId  = senderId
                        ))
                    } catch (e: Exception) { /* ignorar nodos mal formateados */ }
                }
                trySend(messages)
            }
            override fun onCancelled(error: DatabaseError) { trySend(emptyList()) }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
