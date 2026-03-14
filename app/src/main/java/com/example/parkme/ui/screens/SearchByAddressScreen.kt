package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SearchByAddressScreen(navController: NavController) {

    var address by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Search Parking", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Schedule") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {

            navController.navigate("parking_list")

        }) {

            Text("Search")

        }

    }

}