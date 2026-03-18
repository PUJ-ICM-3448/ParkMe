package com.example.parkme.data.mock

import com.example.parkme.data.model.Message

object MockChatData {

    private val chats = mutableMapOf<Int, MutableList<Message>>()

    fun getMessages(parkingId: Int): List<Message> {
        return chats[parkingId] ?: emptyList()
    }

    fun sendMessage(parkingId: Int, message: Message) {

        if (!chats.containsKey(parkingId)) {
            chats[parkingId] = mutableListOf()
        }

        chats[parkingId]?.add(message)
    }
}