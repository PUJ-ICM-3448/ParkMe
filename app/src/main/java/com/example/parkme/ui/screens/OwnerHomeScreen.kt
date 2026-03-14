package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.data.mock.MockReservationData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(navController: NavController) {

    val user = MockAuth.currentUser

    var parkings by remember {
        mutableStateOf(MockParkingData.parkingList)
    }

    val myParkings = parkings.filter {
        it.ownerEmail == user?.email
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("My Parkings") }
            )
        }

    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            items(myParkings) { parking ->

                val reservations =
                    MockReservationData.getReservationsByParking(parking.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            parking.name,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(parking.address)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Available spots: ${parking.availableSpots}"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row {

                            Button(onClick = {

                                parking.availableSpots++
                                parking.totalSpots++

                            }) {

                                Text("+ Spot")

                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(onClick = {

                                if (parking.availableSpots > 0) {
                                    parking.availableSpots--
                                    parking.totalSpots--
                                }

                            }) {

                                Text("- Spot")

                            }

                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Cars parked:",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (reservations.isEmpty()) {

                            Text("No cars parked")

                        } else {

                            reservations.forEach {

                                Text(
                                    "${it.plate} - ${it.userName}"
                                )

                            }

                        }

                    }

                }

            }

        }

    }

}