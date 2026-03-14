package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
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
        Text("Parking no encontrado")
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
                            contentDescription = "Back"
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
                .padding(20.dp)
        ) {

            // Icono grande
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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = parking.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(parking.address)

            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("${parking.pricePerHour} COP por hora")

            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    navController.navigate("${Routes.RESERVATION}/${parking.id}")
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Reservar parqueadero")

            }

        }

    }

}