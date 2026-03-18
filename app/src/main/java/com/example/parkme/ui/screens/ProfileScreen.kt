package com.example.parkme.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.R
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.model.User
import com.example.parkme.navigation.Routes

@Composable
fun ProfileScreen(navController: NavController) {

    val user = MockAuth.currentUser ?: return

    var name by remember { mutableStateOf(user.name) }
    var plate by remember { mutableStateOf(user.plate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //  FOTO PERFIL
        Image(
            painter = painterResource(R.drawable.profile_placeholder),
            contentDescription = null,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { }) {
            Text("Cambiar foto (próximamente)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        //  EMAIL
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        //  CARD DE DATOS
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                //  NOMBRE
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (user.role == "CLIENT") {

                    Spacer(modifier = Modifier.height(12.dp))

                    // PLACA
                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it },
                        label = { Text("Placa del vehículo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                }

            }

        }

        Spacer(modifier = Modifier.height(24.dp))

        // GUARDAR
        Button(
            onClick = {

                val updatedUser = User(
                    name = name,
                    email = user.email,
                    password = user.password,
                    plate = plate,
                    role = user.role
                )

                MockAuth.updateUser(updatedUser)

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {

            Icon(Icons.Default.Save, contentDescription = null)

            Spacer(modifier = Modifier.width(8.dp))

            Text("Guardar cambios")

        }

        Spacer(modifier = Modifier.height(10.dp))

        // LOGOUT
        OutlinedButton(
            onClick = {

                MockAuth.logout()

                navController.navigate(Routes.LOGIN)

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {

            Icon(Icons.Default.Logout, contentDescription = null)

            Spacer(modifier = Modifier.width(8.dp))

            Text("Cerrar sesión")

        }

    }

}