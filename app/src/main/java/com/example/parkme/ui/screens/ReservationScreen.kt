package com.example.parkme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    parkingId: Int,
    parkingName: String
) {
    val context     = LocalContext.current
    val currentUser = MockAuth.currentUser
    val parking     = MockParkingData.getParkingById(parkingId)

    var selectedHours by remember { mutableStateOf(1) }
    var confirmed     by remember { mutableStateOf(false) }
    var lastReservation by remember { mutableStateOf<Reservation?>(null) }

    // ── Selector de hora de inicio ────────────────────────────────────────────
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

    // Hora formateada para mostrar y guardar
    val hourDisplay = String.format("%02d:%02d", selectedHour, selectedMinute)

    // Diálogo TimePicker
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title            = { Text("Hora de inicio") },
            text             = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = timeState)
                }
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
                title = { Text("Reservar — $parkingName") },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (!confirmed) {

                // ── Datos del usuario ─────────────────────────────────────────
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Datos del cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("👤 ${currentUser?.name ?: "—"}")
                        Text("📧 ${currentUser?.email ?: "—"}")
                        if (!currentUser?.plate.isNullOrEmpty())
                            Text("🚗 Placa: ${currentUser?.plate}")
                    }
                }

                // ── Hora de inicio ────────────────────────────────────────────
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Hora de inicio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment    = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier             = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text  = hourDisplay,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            OutlinedButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Cambiar")
                            }
                        }
                    }
                }

                // ── Selección de horas ────────────────────────────────────────
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Duración de la reserva", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(onClick = { if (selectedHours > 1) selectedHours-- }) { Text("−") }
                            Text("$selectedHours hora(s)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = { if (selectedHours < 12) selectedHours++ }) { Text("+") }
                        }
                        Text("Total: $${totalPrice.toInt()} COP", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                        confirmed = true

                        // Notificación de reserva confirmada
                        ParkMeNotificationHelper.showReservationNotification(
                            context     = context,
                            parkingName = parkingName,
                            date        = dateWithHour,
                            hours       = selectedHours
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirmar reserva — $${totalPrice.toInt()} COP")
                }

            } else {

                // ── Confirmación + QR ─────────────────────────────────────────
                Card(
                    shape     = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier  = Modifier.fillMaxWidth(),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier            = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("¡Reserva Confirmada!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(parkingName, style = MaterialTheme.typography.titleMedium)
                        Text("🕐 Hora de inicio: $hourDisplay")
                        Text("$selectedHours hora(s) · $${totalPrice.toInt()} COP")
                    }
                }

                // Botón para ver QR
                lastReservation?.let { reservation ->
                    Button(
                        onClick  = { navController.navigate("${Routes.QR_CODE}/${reservation.id}") },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.QrCode, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ver QR de acceso")
                    }
                }

                OutlinedButton(
                    onClick  = { navController.navigate(Routes.CLIENT_HOME) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Volver al inicio")
                }
            }
        }
    }
}
