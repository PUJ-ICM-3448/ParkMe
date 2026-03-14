package com.example.parkme.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.parkme_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(140.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {

            val success = MockAuth.login(email, password)

            if (success) {

                val role = MockAuth.currentUser?.role

                if (role == "CLIENT") {
                    navController.navigate(Routes.CLIENT_HOME)
                } else {
                    navController.navigate(Routes.OWNER_HOME)
                }

            }

        }) {
            Text("Login")
        }

        TextButton(onClick = {
            navController.navigate(Routes.REGISTER)
        }) {
            Text("Create account")
        }

    }

}