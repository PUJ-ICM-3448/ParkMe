package com.example.parkme.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
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
    var visible   by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Gradiente decorativo superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Ícono de cabecera ─────────────────────────────────────────
                Box(
                    modifier        = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier           = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text       = "Crear cuenta",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = "Únete a ParkMe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(28.dp))

                // ── Formulario ────────────────────────────────────────────────
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        RegisterField(
                            value         = name,
                            onValueChange = { name = it; errorMsg = null },
                            label         = "Nombre completo",
                            icon          = Icons.Default.Person
                        )

                        Spacer(Modifier.height(12.dp))

                        RegisterField(
                            value         = email,
                            onValueChange = { email = it; errorMsg = null },
                            label         = "Correo electrónico",
                            icon          = Icons.Default.Email
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value                = password,
                            onValueChange        = { password = it; errorMsg = null },
                            label                = { Text("Contraseña (mín. 6 caracteres)") },
                            leadingIcon          = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            modifier             = Modifier.fillMaxWidth(),
                            shape                = RoundedCornerShape(14.dp),
                            singleLine           = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors               = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        if (role == "CLIENT") {
                            Spacer(Modifier.height(12.dp))
                            RegisterField(
                                value         = plate,
                                onValueChange = { plate = it },
                                label         = "Placa del vehículo",
                                icon          = Icons.Default.DirectionsCar
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Selector de rol ───────────────────────────────────
                        Text(
                            text       = "Tipo de cuenta",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            RoleChip(
                                label     = "Cliente",
                                icon      = Icons.Default.DirectionsCar,
                                selected  = role == "CLIENT",
                                onClick   = { role = "CLIENT" },
                                modifier  = Modifier.weight(1f)
                            )
                            RoleChip(
                                label    = "Arrendador",
                                icon     = Icons.Default.LocalParking,
                                selected = role == "OWNER",
                                onClick  = { role = "OWNER" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Error
                        if (errorMsg != null) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier          = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text  = errorMsg!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick  = {
                                when {
                                    name.isBlank()      -> { errorMsg = "Ingresa tu nombre"; return@Button }
                                    email.isBlank()     -> { errorMsg = "Ingresa tu correo"; return@Button }
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
                                    result.onSuccess {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape   = RoundedCornerShape(14.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(22.dp),
                                    color       = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Crear cuenta", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                TextButton(onClick = { navController.navigate(Routes.LOGIN) }) {
                    Text("¿Ya tienes cuenta? ")
                    Text(
                        "Inicia sesión",
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ── Campo de texto reutilizable ───────────────────────────────────────────────
@Composable
private fun RegisterField(
    value        : String,
    onValueChange: (String) -> Unit,
    label        : String,
    icon         : androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        leadingIcon   = { Icon(icon, contentDescription = null) },
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(14.dp),
        singleLine    = true,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

// ── Chip de rol ───────────────────────────────────────────────────────────────
@Composable
private fun RoleChip(
    label   : String,
    icon    : androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (selected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(14.dp),
        color    = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier            = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text       = label,
                color      = contentColor,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}