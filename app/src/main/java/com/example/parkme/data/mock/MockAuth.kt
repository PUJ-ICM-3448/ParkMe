package com.example.parkme.data.mock

import com.example.parkme.data.model.User

object MockAuth {

    var currentUser: User? = null

    private val users = mutableListOf<User>()

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

}