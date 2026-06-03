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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.R
import com.example.parkme.data.firebase.FirebaseAuthRepository
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth().padding(24.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter            = painterResource(id = R.drawable.parkme_logo),
                    contentDescription = "Logo",
                    modifier           = Modifier.size(120.dp)
                )

                Spacer(Modifier.height(16.dp))
                Text("Bienvenido a ParkMe", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it; errorMsg = null },
                    label         = { Text("Correo") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value               = password,
                    onValueChange       = { password = it; errorMsg = null },
                    label               = { Text("Contraseña") },
                    modifier            = Modifier.fillMaxWidth(),
                    singleLine          = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                // Mensaje de error
                if (errorMsg != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick  = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMsg = "Completa todos los campos"
                            return@Button
                        }
                        isLoading = true
                        errorMsg  = null
                        scope.launch {
                            val result = FirebaseAuthRepository.login(email.trim(), password)
                            isLoading = false
                            result.onSuccess { user ->
                                // Sincronizar con MockAuth para mantener compatibilidad
                                MockAuth.setCurrentUser(user)
                                if (user.role == "CLIENT") navController.navigate(Routes.CLIENT_HOME)
                                else                       navController.navigate(Routes.OWNER_HOME)
                            }.onFailure { e ->
                                errorMsg = when {
                                    e.message?.contains("no user record") == true   -> "No existe una cuenta con ese correo"
                                    e.message?.contains("password is invalid") == true -> "Contraseña incorrecta"
                                    e.message?.contains("network") == true          -> "Sin conexión a internet"
                                    else -> "Error al iniciar sesión. Verifica tus datos."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color    = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Iniciar sesión")
                    }
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = { navController.navigate(Routes.REGISTER) }) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}