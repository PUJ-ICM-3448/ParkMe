package com.example.parkme.data.mock

import com.example.parkme.data.model.User

object MockAuth {

    var currentUser: User? = null

    private val users = mutableListOf(

        User("Juan Perez","juan@mail.com","1234","ABC123","CLIENT"),
        User("Maria Lopez","maria@mail.com","1234","XYZ987","CLIENT"),

        User("Carlos Owner","owner@mail.com","1234","","OWNER"),
        User("Laura Parking","owner2@mail.com","1234","","OWNER")

    )

    fun register(user: User): Boolean {
        users.add(user)
        currentUser = user
        return true
    }

    fun login(email: String, password: String): Boolean {

        val user = users.find {
            it.email == email && it.password == password
        }

        currentUser = user
        return user != null
    }

    fun logout() {
        currentUser = null
    }

    fun updateUser(updatedUser: User) {

        val index = users.indexOfFirst {
            it.email == updatedUser.email
        }

        if (index != -1) {
            users[index] = updatedUser
        }

        currentUser = updatedUser
    }
}