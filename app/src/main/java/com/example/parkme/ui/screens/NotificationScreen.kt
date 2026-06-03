package com.example.parkme.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.parkme.data.mock.*
import com.example.parkme.notifications.ParkMeNotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {

    val context = LocalContext.current
    val user    = MockAuth.currentUser ?: return

    val notifications = if (user.role == "CLIENT")
        MockNotificationData.getNotificationsForUser(user.email)
    else
        MockNotificationData.getNotificationsForOwner(user.email)

    // Pedir permiso de notificaciones si no se ha pedido
    var permissionAsked by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionAsked = true }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionAsked) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Botón para enviar una notificación de prueba
                    IconButton(onClick = {
                        ParkMeNotificationHelper.showReservationNotification(
                            context     = context,
                            parkingName = "Parqueadero Central",
                            date        = "hoy",
                            hours       = 2
                        )
                    }) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Probar notificación",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (notifications.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, null, modifier = Modifier.size(60.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No tienes notificaciones", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Aquí verás novedades de tus reservas", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    // Botón de prueba si no hay notificaciones
                    OutlinedButton(onClick = {
                        ParkMeNotificationHelper.showChatNotification(
                            context     = context,
                            senderName  = "Sistema ParkMe",
                            messageText = "Tienes una reserva próxima",
                            parkingName = "Parqueadero Demo"
                        )
                    }) {
                        Icon(Icons.Default.NotificationsActive, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Enviar notificación de prueba")
                    }
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
                        modifier  = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape     = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(5.dp)
                    ) {
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, null)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notification.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(notification.time, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
