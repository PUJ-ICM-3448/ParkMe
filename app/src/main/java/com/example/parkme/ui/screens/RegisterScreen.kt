package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.model.User
import com.example.parkme.navigation.Routes

@Composable
fun RegisterScreen(navController: NavController) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }

    var role by remember { mutableStateOf("CLIENT") }

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

                //  TÍTULO
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(Icons.Default.PersonAdd, contentDescription = null)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Crear cuenta",
                        style = MaterialTheme.typography.titleLarge
                    )

                }

                Spacer(modifier = Modifier.height(20.dp))

                //  NOMBRE
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                //  PASSWORD
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth()
                )

                // PLACA SOLO CLIENT
                if (role == "CLIENT") {

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it },
                        label = { Text("Placa del vehículo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                }

                Spacer(modifier = Modifier.height(20.dp))

                // TIPO DE CUENTA
                Text(
                    text = "Tipo de cuenta",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    RadioButton(
                        selected = role == "CLIENT",
                        onClick = { role = "CLIENT" }
                    )

                    Text("Cliente")

                    Spacer(modifier = Modifier.width(20.dp))

                    RadioButton(
                        selected = role == "OWNER",
                        onClick = { role = "OWNER" }
                    )

                    Text("Owner")

                }

                Spacer(modifier = Modifier.height(24.dp))

                // BOTÓN REGISTER
                Button(
                    onClick = {

                        val finalPlate =
                            if (role == "CLIENT") plate else ""

                        MockAuth.register(
                            User(name, email, password, finalPlate, role)
                        )

                        if (role == "CLIENT")
                            navController.navigate(Routes.CLIENT_HOME)
                        else
                            navController.navigate(Routes.OWNER_HOME)

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {

                    Icon(Icons.Default.PersonAdd, contentDescription = null)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Crear cuenta")

                }

                Spacer(modifier = Modifier.height(10.dp))

                // VOLVER LOGIN
                TextButton(onClick = {
                    navController.navigate(Routes.LOGIN)
                }) {
                    Text("Ya tengo cuenta")
                }

            }

        }

    }

}