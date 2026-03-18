package com.example.parkme.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.*
import com.example.parkme.navigation.Routes
import com.example.parkme.ui.components.AppDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(navController: NavController) {

    val user = MockAuth.currentUser

    val myParkings = MockParkingData.parkingList.filter {
        it.ownerEmail == user?.email
    }

    AppDrawer(navController) {

        Scaffold(

            topBar = {
                TopAppBar(

                    title = { Text("ParkMe Owner") },

                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigate(Routes.PROFILE)
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    },

                    actions = {

                        IconButton(onClick = {
                            navController.navigate(Routes.NOTIFICATIONS)
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }

                        IconButton(onClick = {
                            navController.navigate(Routes.ADD_PARKING)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }

                    }

                )
            }

        ) { padding ->

            // 🧠 ESTADO VACÍO PRO
            if (myParkings.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "No tienes parqueaderos",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Empieza creando uno 🚀",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(onClick = {
                            navController.navigate(Routes.ADD_PARKING)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crear parqueadero")
                        }

                    }

                }

                return@Scaffold
            }

            // 🚀 LISTA PRO
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {

                items(myParkings) { parking ->

                    val reservations =
                        MockReservationData.getReservationsByParking(parking.id)

                    val available =
                        parking.totalSpaces - parking.occupiedSpaces

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clickable {
                                navController.navigate("${Routes.OWNER_PARKING_DETAIL}/${parking.id}")
                            },

                        shape = RoundedCornerShape(16.dp),

                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            // 🧠 NOMBRE + DIRECCIÓN
                            Text(
                                text = parking.name,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = parking.address,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 📊 DISPONIBILIDAD
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(Icons.Default.CheckCircle, contentDescription = null)

                                Spacer(modifier = Modifier.width(6.dp))

                                Text("Disponibles: $available / ${parking.totalSpaces}")

                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // ➕ ➖ BOTONES MEJORADOS
                            Row {

                                OutlinedButton(onClick = {
                                    parking.totalSpaces++
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cupo")
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                OutlinedButton(onClick = {
                                    if (parking.totalSpaces > parking.occupiedSpaces)
                                        parking.totalSpaces--
                                }) {
                                    Icon(Icons.Default.Remove, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cupo")
                                }

                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 📩 RESERVAS
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(Icons.Default.List, contentDescription = null)

                                Spacer(modifier = Modifier.width(6.dp))

                                Text("Reservas: ${reservations.size}")

                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Toca para ver clientes y chatear",
                                style = MaterialTheme.typography.bodySmall
                            )

                        }

                    }

                }

            }

        }

    }

}