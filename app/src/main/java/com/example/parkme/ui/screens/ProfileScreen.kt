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
import com.example.parkme.R
import com.example.parkme.data.mock.MockAuth

@Composable
fun ProfileScreen() {

    val user = MockAuth.currentUser

    var plate by remember { mutableStateOf(user?.plate ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.profile_placeholder),
            contentDescription = "profile",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(user?.name ?: "", style = MaterialTheme.typography.headlineSmall)

        Text(user?.email ?: "")

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = plate,
            onValueChange = { plate = it },
            label = { Text("Vehicle plate") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {

            user?.let {
                MockAuth.currentUser =
                    it.copy(plate = plate)
            }

        }) {

            Text("Save plate")

        }

    }

}