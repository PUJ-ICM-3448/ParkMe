package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.firebase.FirebaseAuthRepository
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.model.User
import com.example.parkme.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {

    var name      by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var plate     by remember { mutableStateOf("") }
    var role      by remember { mutableStateOf("CLIENT") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Box(
        modifier            = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment    = Alignment.Center
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth().padding(24.dp),
            shape     = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Crear cuenta", style = MaterialTheme.typography.titleLarge)
                }

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it; errorMsg = null },
                    label         = { Text("Nombre completo") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it; errorMsg = null },
                    label         = { Text("Correo electrónico") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; errorMsg = null },
                    label                = { Text("Contraseña (mínimo 6 caracteres)") },
                    modifier             = Modifier.fillMaxWidth(),
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                if (role == "CLIENT") {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = plate,
                        onValueChange = { plate = it },
                        label         = { Text("Placa del vehículo") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text("Tipo de cuenta", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == "CLIENT", onClick = { role = "CLIENT" })
                    Text("Cliente")
                    Spacer(Modifier.width(20.dp))
                    RadioButton(selected = role == "OWNER", onClick = { role = "OWNER" })
                    Text("Dueño de parqueadero")
                }

                // Error
                if (errorMsg != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick  = {
                        // Validaciones locales primero
                        when {
                            name.isBlank()     -> { errorMsg = "Ingresa tu nombre"; return@Button }
                            email.isBlank()    -> { errorMsg = "Ingresa tu correo"; return@Button }
                            password.length < 6 -> { errorMsg = "La contraseña debe tener al menos 6 caracteres"; return@Button }
                            role == "CLIENT" && plate.isBlank() -> { errorMsg = "Ingresa la placa de tu vehículo"; return@Button }
                        }
                        isLoading = true
                        errorMsg  = null
                        scope.launch {
                            val finalPlate = if (role == "CLIENT") plate else ""
                            val result = FirebaseAuthRepository.register(
                                name     = name.trim(),
                                email    = email.trim(),
                                password = password,
                                plate    = finalPlate.trim().uppercase(),
                                role     = role
                            )
                            isLoading = false
                            result.onSuccess { _ ->
                                // El registro cierra la sesión de Firebase automáticamente
                                // (ver FirebaseAuthRepository.register) para no desplazar
                                // la sesión activa del dueño u otro usuario logueado.
                                // Redirigir a login para que el nuevo usuario ingrese explícitamente.
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.REGISTER) { inclusive = true }
                                }
                            }.onFailure { e ->
                                errorMsg = when {
                                    e.message?.contains("email address is already") == true -> "Ya existe una cuenta con ese correo"
                                    e.message?.contains("badly formatted") == true          -> "Formato de correo inválido"
                                    e.message?.contains("network") == true                  -> "Sin conexión a internet"
                                    else -> "Error al crear la cuenta. Intenta de nuevo."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled  = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Crear cuenta")
                    }
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = { navController.navigate(Routes.LOGIN) }) {
                    Text("Ya tengo cuenta")
                }
            }
        }
    }
}