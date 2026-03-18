package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.example.parkme.data.mock.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {

    val user = MockAuth.currentUser ?: return

    val notifications =
        if (user.role == "CLIENT")
            MockNotificationData.getNotificationsForUser(user.email)
        else
            MockNotificationData.getNotificationsForOwner(user.email)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") }
            )
        }
    ) { padding ->

        if (notifications.isEmpty()) {

            //ESTADO VACÍO
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No tienes notificaciones",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Aquí verás novedades de tus reservas",
                        style = MaterialTheme.typography.bodyMedium
                    )

                }

            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {

                items(notifications) { notification ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),

                        shape = RoundedCornerShape(16.dp),

                        elevation = CardDefaults.cardElevation(5.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = notification.text,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = notification.time,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                }

                            }

                        }

                    }

                }

            }

        }

    }

}