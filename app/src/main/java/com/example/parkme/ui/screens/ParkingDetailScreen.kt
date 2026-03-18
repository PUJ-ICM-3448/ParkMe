package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingDetailScreen(
    navController: NavController,
    parkingId: String
) {

    val parking = MockParkingData.getParkingById(parkingId.toInt())

    if (parking == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Parking no encontrado")
        }
        return
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Detalle del parqueadero")
                },

                navigationIcon = {

                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )

                    }

                }

            )

        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // 🧠 HEADER VISUAL
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(50.dp)
                )

            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = parking.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 📍 DIRECCIÓN CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(Icons.Default.Place, contentDescription = null)

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(parking.address)

                }

            }

            Spacer(modifier = Modifier.height(12.dp))

            // 💰 PRECIO CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(Icons.Default.AttachMoney, contentDescription = null)

                    Spacer(modifier = Modifier.width(10.dp))

                    Text("${parking.pricePerHour} COP por hora")

                }

            }

            Spacer(modifier = Modifier.height(30.dp))

            // 📊 DISPONIBILIDAD
            val available = parking.totalSpaces - parking.occupiedSpaces

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(Icons.Default.CheckCircle, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text("Disponibles: $available / ${parking.totalSpaces}")

            }

            Spacer(modifier = Modifier.weight(1f))

            // 🚀 BOTÓN PRINCIPAL (CTA)
            Button(
                onClick = {
                    navController.navigate("${Routes.RESERVATION}/${parking.id}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {

                Icon(Icons.Default.LocalParking, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text("Reservar parqueadero")

            }

        }

    }

}