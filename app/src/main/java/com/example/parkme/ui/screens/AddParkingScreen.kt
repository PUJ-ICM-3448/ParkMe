package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.data.model.Parking
import com.example.parkme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParkingScreen(navController: NavController) {

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var spaces by remember { mutableStateOf("") }

    val user = MockAuth.currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Parqueadero") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(20.dp)
        ) {

            Text("Información", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio por hora") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(value = spaces, onValueChange = { spaces = it }, label = { Text("Cupos") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (user != null) {
                        val newParking = Parking(
                            id = System.currentTimeMillis().toInt(),
                            name = name,
                            address = address,
                            pricePerHour = price.toDoubleOrNull() ?: 0.0,
                            ownerEmail = user.email,
                            totalSpaces = spaces.toIntOrNull() ?: 0,
                            occupiedSpaces = 0
                        )
                        MockParkingData.addParking(newParking)
                        navController.navigate(Routes.OWNER_HOME)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear parqueadero")
            }
        }
    }
}