package com.example.parkme.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.data.mock.MockReservationData
import com.example.parkme.data.model.Reservation
import com.example.parkme.navigation.Routes
import com.example.parkme.notifications.ParkMeNotificationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    navController: NavController,
    parkingId    : Int,
    parkingName  : String
) {
    val context     = LocalContext.current
    val currentUser = MockAuth.currentUser
    val parking     = MockParkingData.getParkingById(parkingId)

    var selectedHours   by remember { mutableStateOf(1) }
    var confirmed       by remember { mutableStateOf(false) }
    var lastReservation by remember { mutableStateOf<Reservation?>(null) }

    val now = Calendar.getInstance()
    var selectedHour   by remember { mutableStateOf(now.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(now.get(Calendar.MINUTE)) }
    var showTimePicker  by remember { mutableStateOf(false) }

    val timeState = rememberTimePickerState(
        initialHour   = selectedHour,
        initialMinute = selectedMinute,
        is24Hour      = true
    )

    val pricePerHour = parking?.pricePerHour ?: 5000.0
    val totalPrice   = pricePerHour * selectedHours
    val today        = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val hourDisplay  = String.format("%02d:%02d", selectedHour, selectedMinute)

    // Diálogo TimePicker
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title            = { Text("Hora de inicio") },
            text             = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) { TimePicker(state = timeState) }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour   = timeState.hour
                    selectedMinute = timeState.minute
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Reservar",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            parkingName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (!confirmed) {

                // ── Datos del cliente ─────────────────────────────────────────
                ReservationSectionCard(title = "Datos del cliente") {
                    ReservationInfoRow(Icons.Default.Person, currentUser?.name ?: "—")
                    Spacer(Modifier.height(8.dp))
                    ReservationInfoRow(Icons.Default.Email, currentUser?.email ?: "—")
                    if (!currentUser?.plate.isNullOrEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        ReservationInfoRow(Icons.Default.DirectionsCar, "Placa: ${currentUser?.plate}")
                    }
                }

                // ── Hora de inicio ────────────────────────────────────────────
                ReservationSectionCard(title = "Hora de inicio") {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier        = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier           = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text       = hourDisplay,
                                style      = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            shape   = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cambiar")
                        }
                    }
                }

                // ── Duración ──────────────────────────────────────────────────
                ReservationSectionCard(title = "Duración") {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        FilledTonalIconButton(
                            onClick = { if (selectedHours > 1) selectedHours-- },
                            shape   = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text       = "$selectedHours",
                                style      = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text  = if (selectedHours == 1) "hora" else "horas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalIconButton(
                            onClick = { if (selectedHours < 12) selectedHours++ },
                            shape   = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más")
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Total a pagar",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text       = "$${totalPrice.toInt()} COP",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ── Botón Confirmar ───────────────────────────────────────────
                Button(
                    onClick = {
                        val dateWithHour = "$today $hourDisplay"
                        val reservation = Reservation(
                            id          = System.currentTimeMillis().toInt(),
                            userId      = currentUser?.email ?: "",
                            userName    = currentUser?.name ?: "",
                            parkingId   = parkingId,
                            parkingName = parkingName,
                            date        = today,
                            hour        = hourDisplay,
                            hours       = selectedHours,
                            totalPrice  = totalPrice,
                            status      = "Confirmada",
                            plate       = currentUser?.plate ?: ""
                        )
                        MockReservationData.addReservation(reservation)
                        lastReservation = reservation
                        confirmed       = true
                        ParkMeNotificationHelper.showReservationNotification(
                            context     = context,
                            parkingName = parkingName,
                            date        = dateWithHour,
                            hours       = selectedHours
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Confirmar — $${totalPrice.toInt()} COP",
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.titleSmall
                    )
                }

            } else {

                // ── Confirmación animada ──────────────────────────────────────
                AnimatedVisibility(
                    visible = confirmed,
                    enter   = fadeIn() + scaleIn()
                ) {
                    Card(
                        shape   = RoundedCornerShape(24.dp),
                        colors  = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier        = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    modifier = Modifier.size(40.dp),
                                    tint     = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "¡Reserva Confirmada!",
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                parkingName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ConfirmationDetail(
                                    icon  = Icons.Default.Schedule,
                                    value = hourDisplay,
                                    label = "Hora inicio"
                                )
                                ConfirmationDetail(
                                    icon  = Icons.Default.Timelapse,
                                    value = "$selectedHours h",
                                    label = "Duración"
                                )
                                ConfirmationDetail(
                                    icon  = Icons.Default.AttachMoney,
                                    value = "$${totalPrice.toInt()}",
                                    label = "Total COP"
                                )
                            }
                        }
                    }
                }

                lastReservation?.let { reservation ->
                    Button(
                        onClick  = { navController.navigate("${Routes.QR_CODE}/${reservation.id}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.QrCode, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ver QR de acceso", fontWeight = FontWeight.SemiBold)
                    }
                }

                OutlinedButton(
                    onClick  = { navController.navigate(Routes.CLIENT_HOME) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Home, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Volver al inicio")
                }
            }
        }
    }
}

// ── Tarjeta de sección ────────────────────────────────────────────────────────
@Composable
fun ReservationSectionCard(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── Fila de información ───────────────────────────────────────────────────────
@Composable
fun ReservationInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

// ── Detalle en confirmación ───────────────────────────────────────────────────
@Composable
fun ConfirmationDetail(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}