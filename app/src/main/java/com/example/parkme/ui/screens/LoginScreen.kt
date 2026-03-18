package com.example.parkme.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.R
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.navigation.Routes

@Composable
fun LoginScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),

            elevation = CardDefaults.cardElevation(8.dp)
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                //  LOGO
                Image(
                    painter = painterResource(id = R.drawable.parkme_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // TÍTULO
                Text(
                    text = "Bienvenido a ParkMe",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                //  Correo
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                //  Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // BOTÓN LOGIN
                Button(
                    onClick = {

                        val success = MockAuth.login(email, password)

                        if (success) {

                            val role = MockAuth.currentUser?.role

                            if (role == "CLIENT") {
                                navController.navigate(Routes.CLIENT_HOME)
                            } else {
                                navController.navigate(Routes.OWNER_HOME)
                            }

                        }

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar sesión")
                }

                Spacer(modifier = Modifier.height(10.dp))

                //  REGISTRO
                TextButton(onClick = {
                    navController.navigate(Routes.REGISTER)
                }) {
                    Text("Crear cuenta")
                }

            }

        }

    }

}