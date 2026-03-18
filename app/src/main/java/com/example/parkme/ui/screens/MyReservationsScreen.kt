package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.*
import com.example.parkme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(navController: NavController) {

    val user = MockAuth.currentUser

    val reservations = MockReservationData
        .getAllReservations()
        .filter { it.userName == user?.name }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reservas") }
            )
        }
    ) { padding ->

        if (reservations.isEmpty()) {

            // 🧠 ESTADO VACÍO (MUY PRO)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = "No tienes reservas",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Busca un parqueadero y reserva fácilmente 🚗",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = {
                        navController.navigate(Routes.PARKING_LIST)
                    }) {
                        Text("Buscar parqueaderos")
                    }

                }

            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {

                items(reservations) { reservation ->

                    val parking =
                        MockParkingData.getParkingById(reservation.parkingId)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),

                        shape = RoundedCornerShape(16.dp),

                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            // 🧠 NOMBRE DEL PARQUEADERO
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    imageVector = Icons.Default.LocalParking,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = parking?.name ?: "Parqueadero",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 🚗 PLACA
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Placa: ${reservation.plate}")
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // ⏰ HORA
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Hora: ${reservation.hour}")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 💬 BOTÓN CHAT
                            Button(
                                onClick = {
                                    navController.navigate("${Routes.CHAT}/${reservation.parkingId}")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Icon(Icons.Default.Chat, contentDescription = null)

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Ir al chat")

                            }

                        }

                    }

                }

            }

        }

    }

}