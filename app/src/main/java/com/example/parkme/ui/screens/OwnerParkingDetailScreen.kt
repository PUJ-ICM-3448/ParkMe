package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerParkingDetailScreen(
    navController: NavController,
    parkingId: Int
) {

    val parking = MockParkingData.getParkingById(parkingId)

    val reservations =
        MockReservationData.getReservationsByParking(parkingId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(parking?.name ?: "Parking") }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            //  HEADER DEL PARQUEADERO
            item {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),

                    shape = RoundedCornerShape(16.dp),

                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = parking?.name ?: "Parqueadero",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = parking?.address ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Icon(Icons.Default.List, contentDescription = null)

                            Spacer(modifier = Modifier.width(6.dp))

                            Text("Reservas: ${reservations.size}")

                        }

                    }

                }

            }

            //  ESTADO VACÍO
            if (reservations.isEmpty()) {

                item {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "No hay reservas aún",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Cuando alguien reserve aparecerá aquí",
                            style = MaterialTheme.typography.bodyMedium
                        )

                    }

                }

            } else {

                //  LISTA DE CLIENTES
                items(reservations) { reservation ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),

                        shape = RoundedCornerShape(16.dp),

                        elevation = CardDefaults.cardElevation(5.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            // CLIENTE
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(Icons.Default.Person, contentDescription = null)

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = reservation.userName,
                                    style = MaterialTheme.typography.titleMedium
                                )

                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            //  PLACA
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(Icons.Default.DirectionsCar, contentDescription = null)

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Placa: ${reservation.plate}")

                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            //  HORA
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(Icons.Default.Schedule, contentDescription = null)

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Hora: ${reservation.hour}")

                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 💬 CHAT
                            Button(
                                onClick = {
                                    navController.navigate("chat/${parkingId}")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Icon(Icons.Default.Chat, contentDescription = null)

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Chatear con cliente")

                            }

                        }

                    }

                }

            }

        }

    }

}