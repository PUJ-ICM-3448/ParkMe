package com.example.parkme.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(R.drawable.profile_placeholder),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )

        if(user.role == "CLIENT"){

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it },
                label = { Text("Vehicle plate") }
            )

        }

        Spacer(modifier = Modifier.height(20.dp))

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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save changes")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {

                MockAuth.logout()

                navController.navigate(Routes.LOGIN)

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

    }

}