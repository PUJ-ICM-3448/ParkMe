package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.*
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
            TopAppBar(title = { Text("Reserva") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
        ) {

            Text("Reservar parqueadero")

            Spacer(modifier = Modifier.height(20.dp))

            Text(parkingName)

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {

                    val user = MockAuth.currentUser
                    val parking =
                        MockParkingData.getParkingById(parkingId)

                    if (user != null && parking != null) {

                        val available =
                            parking.totalSpaces - parking.occupiedSpaces

                        if (available > 0) {

                            val reservation = Reservation(
                                id = System.currentTimeMillis().toInt(),
                                parkingId = parkingId,
                                userName = user.name,
                                plate = user.plate
                            )

                            MockReservationData.addReservation(reservation)

                            parking.occupiedSpaces++
                        }
                    }

                    navController.navigate(Routes.CLIENT_HOME)

                }
            ) {
                Text("Confirmar reserva")
            }
        }
    }
}