package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SearchByAddressScreen(navController: NavController) {

    var address by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),

            shape = RoundedCornerShape(20.dp),

            elevation = CardDefaults.cardElevation(8.dp)
        ) {

            Column(
                modifier = Modifier.padding(24.dp)
            ) {

                // 🧠 TÍTULO
                Text(
                    text = "Buscar parqueadero",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 📍 DIRECCIÓN
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 🕒 HORA
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Hora (Ej: 14:00)") },
                    leadingIcon = {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔍 BOTÓN BUSCAR
                Button(
                    onClick = {
                        navController.navigate("parking_list")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {

                    Icon(Icons.Default.Search, contentDescription = null)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Buscar")

                }

            }

        }

    }

}