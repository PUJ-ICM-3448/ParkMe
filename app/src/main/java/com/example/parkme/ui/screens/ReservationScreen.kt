package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.mock.MockReservationData
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.data.model.Reservation
import com.example.parkme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    navController: NavController,
    parkingId: Int,
    parkingName: String
) {

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("Reserva") }
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(20.dp)
        ) {

            Text(
                text = "Reservar parqueadero",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = parkingName,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {

                    val user = MockAuth.currentUser

                    val parking =
                        MockParkingData.parkingList.find { it.id == parkingId }

                    if (user != null && parking != null && parking.availableSpots > 0) {

                        val reservation = Reservation(
                            id = System.currentTimeMillis().toInt(),
                            parkingId = parkingId,
                            userName = user.name,
                            plate = user.plate
                        )

                        MockReservationData.addReservation(reservation)

                        parking.availableSpots--

                    }

                    navController.navigate(Routes.CLIENT_HOME)

                }
            ) {

                Text("Confirmar reserva")

            }

        }

    }

}