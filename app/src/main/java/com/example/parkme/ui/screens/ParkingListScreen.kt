package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.components.ParkingCard
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingListScreen(navController: NavController) {

    val parkingList = MockParkingData.parkingList

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("Parqueaderos cercanos") }
            )
        }

    ) { paddingValues ->

        if (parkingList.isEmpty()) {


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No hay parqueaderos disponibles",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Intenta buscar en otra zona ",
                        style = MaterialTheme.typography.bodyMedium
                    )

                }

            }

        } else {

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {

                //  HEADER (detalle pro)
                Text(
                    text = "Encuentra tu parqueadero",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(parkingList) { parking ->

                        ParkingCard(
                            parking = parking,
                            onReserveClick = {
                                navController.navigate("${Routes.PARKING_DETAIL}/${parking.id}")
                            }
                        )

                    }

                }

            }

        }

    }

}