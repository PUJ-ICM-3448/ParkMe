package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Create Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(name, { name = it }, label = { Text("Name") })

        OutlinedTextField(email, { email = it }, label = { Text("Email") })

        OutlinedTextField(password, { password = it }, label = { Text("Password") })

        OutlinedTextField(plate, { plate = it }, label = { Text("Vehicle plate") })

        Spacer(modifier = Modifier.height(20.dp))

        Text("Account type")

        Row {

            RadioButton(
                selected = role == "CLIENT",
                onClick = { role = "CLIENT" }
            )

            Text("Client")

            Spacer(modifier = Modifier.width(20.dp))

            RadioButton(
                selected = role == "OWNER",
                onClick = { role = "OWNER" }
            )

            Text("Owner")

        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {

            MockAuth.register(
                User(name, email, password, plate, role)
            )

            if (role == "CLIENT")
                navController.navigate(Routes.CLIENT_HOME)
            else
                navController.navigate(Routes.OWNER_HOME)

        }) {

            Text("Register")

        }

    }

}