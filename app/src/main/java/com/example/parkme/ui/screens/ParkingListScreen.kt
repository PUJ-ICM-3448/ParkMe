package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
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