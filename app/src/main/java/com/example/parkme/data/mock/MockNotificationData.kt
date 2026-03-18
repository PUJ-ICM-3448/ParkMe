package com.example.parkme.data.mock

import com.example.parkme.data.model.AppNotification

object MockNotificationData {

    private val notifications = mutableListOf<AppNotification>()

    fun addNotification(notification: AppNotification) {
        notifications.add(notification)
    }

    fun getNotifications(): List<AppNotification> {
        return notifications.reversed()
    }
}