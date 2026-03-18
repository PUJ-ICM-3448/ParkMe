package com.example.parkme.data.mock

import com.example.parkme.data.model.AppNotification

object MockNotificationData {

    private val notifications = mutableListOf<AppNotification>()

    fun addNotification(notification: AppNotification) {
        notifications.add(notification)
    }

    fun getNotificationsForUser(email: String): List<AppNotification> {
        return notifications.filter { it.userEmail == email }.reversed()
    }

    fun getNotificationsForOwner(email: String): List<AppNotification> {
        return notifications.filter { it.ownerEmail == email }.reversed()
    }
}