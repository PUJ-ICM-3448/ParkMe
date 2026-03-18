package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.parkme.data.mock.*
import com.example.parkme.data.model.*
import com.example.parkme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    navController: NavController,
    parkingId: Int,
    parkingName: String
) {

    var hour by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar reserva") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {

            //  CARD PARKING
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Parqueadero",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = parkingName,
                        style = MaterialTheme.typography.titleMedium
                    )

                }

            }

            Spacer(modifier = Modifier.height(24.dp))

            //  INPUT HORA
            OutlinedTextField(
                value = hour,
                onValueChange = { hour = it },
                label = { Text("Hora (Ej: 14:00)") },
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            //  BOTÓN CONFIRMAR
            Button(
                onClick = {

                    val user = MockAuth.currentUser
                    val parking = MockParkingData.getParkingById(parkingId)

                    if (user == null || parking == null || hour.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Completa todos los campos")
                        }
                        return@Button
                    }

                    val availableSpaces =
                        parking.totalSpaces - parking.occupiedSpaces

                    if (availableSpaces <= 0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("No hay cupos disponibles")
                        }
                        return@Button
                    }

                    val reservation = Reservation(
                        id = System.currentTimeMillis().toInt(),
                        parkingId = parkingId,
                        userName = user.name,
                        plate = user.plate,
                        hour = hour
                    )

                    MockReservationData.addReservation(reservation)
                    parking.occupiedSpaces++

                    //  NOTIFICACIÓN CORREGIDA
                    MockNotificationData.addNotification(
                        AppNotification(
                            id = System.currentTimeMillis().toInt(),
                            text = "${user.name} reservó en ${parking.name}",
                            time = hour,
                            userEmail = user.email,
                            ownerEmail = parking.ownerEmail
                        )
                    )

                    navController.navigate(Routes.CLIENT_HOME)

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {

                Icon(Icons.Default.CheckCircle, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text("Confirmar reserva")

            }

        }

    }
}