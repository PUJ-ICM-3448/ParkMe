package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(navController: NavController) {

    val user = MockAuth.currentUser

    val myParkings = MockParkingData.parkingList.filter {
        it.ownerEmail == user?.email
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Parkings") })
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            items(myParkings) { parking ->

                val reservations =
                    MockReservationData.getReservationsByParking(parking.id)

                val available =
                    parking.totalSpaces - parking.occupiedSpaces

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    Column(Modifier.padding(16.dp)) {

                        Text(parking.name, style = MaterialTheme.typography.titleLarge)
                        Text(parking.address)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Available spots: $available")

                        Spacer(modifier = Modifier.height(10.dp))

                        Row {

                            Button(onClick = { parking.totalSpaces++ }) {
                                Text("+ Spot")
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(onClick = {
                                if (parking.totalSpaces > parking.occupiedSpaces)
                                    parking.totalSpaces--
                            }) {
                                Text("- Spot")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Cars parked:")

                        if (reservations.isEmpty()) {
                            Text("No cars parked")
                        } else {
                            reservations.forEach {
                                Text("${it.plate} - ${it.userName}")
                            }
                        }
                    }
                }
            }
        }
    }
}