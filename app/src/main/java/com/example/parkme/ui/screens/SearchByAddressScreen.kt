package com.example.parkme.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SearchByAddressScreen(navController: NavController) {

    var address by remember { mutableStateOf("") }
    var time    by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Decoración de fondo: gradiente suave en la parte superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
        ) {
            ElevatedCard(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape     = RoundedCornerShape(28.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp)
                ) {

                    // ── Header ────────────────────────────────────────────────
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier        = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Search,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text       = "Buscar parqueadero",
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text  = "Encuentra el más cercano",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Campo Dirección ───────────────────────────────────────
                    Text(
                        text       = "Dirección",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value         = address,
                        onValueChange = { address = it },
                        placeholder   = { Text("Ej: Calle 100 # 15-20, Bogotá") },
                        leadingIcon   = {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Campo Hora ────────────────────────────────────────────
                    Text(
                        text       = "Hora de llegada",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value         = time,
                        onValueChange = { time = it },
                        placeholder   = { Text("Ej: 14:00") },
                        leadingIcon   = {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(32.dp))

                    // ── Botón Buscar ──────────────────────────────────────────
                    Button(
                        onClick  = { navController.navigate("parking_list") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape    = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text       = "Buscar parqueadero",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}