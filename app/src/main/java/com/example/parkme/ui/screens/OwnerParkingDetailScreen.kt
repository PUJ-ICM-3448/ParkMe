package com.example.parkme.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.*
import com.example.parkme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerParkingDetailScreen(
    navController: NavController,
    parkingId    : Int
) {
    val parking      = MockParkingData.getParkingById(parkingId)
    val reservations = MockReservationData.getReservationsByParking(parkingId)

    // ── Set de IDs de reservas ya verificadas (en memoria, sin Firebase) ──────
    // Persiste mientras la pantalla está en el back-stack.
    val verifiedIds = remember { mutableStateSetOf<Int>() }

    // ── Leer resultado del escáner cuando vuelve de OwnerQrScannerScreen ──────
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(Unit) {
        savedStateHandle?.getLiveData<Int>("verified_reservation_id")
            ?.observeForever { id ->
                if (id != null) {
                    verifiedIds.add(id)
                    // Limpiar para no re-procesar
                    savedStateHandle.remove<Int>("verified_reservation_id")
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        parking?.name ?: "Parking",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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

        LazyColumn(
            modifier            = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding      = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Header del parqueadero ────────────────────────────────────────
            item {
                ParkingDetailHeaderCard(
                    name         = parking?.name ?: "Parqueadero",
                    address      = parking?.address ?: "",
                    reservations = reservations.size,
                    available    = (parking?.totalSpaces ?: 0) - (parking?.occupiedSpaces ?: 0),
                    totalSpaces  = parking?.totalSpaces ?: 0
                )
            }

            // ── Estado vacío ──────────────────────────────────────────────────
            if (reservations.isEmpty()) {
                item {
                    EmptyReservationsState()
                }
            } else {

                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = "Clientes con reserva",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val verifiedCount = reservations.count { it.id in verifiedIds }
                        if (verifiedCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text     = "$verifiedCount verificado${if (verifiedCount > 1) "s" else ""}",
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ── Lista de clientes ─────────────────────────────────────────
                items(reservations) { reservation ->
                    ReservationClientCard(
                        userName     = reservation.userName,
                        plate        = reservation.plate,
                        hour         = reservation.hour,
                        isVerified   = reservation.id in verifiedIds,
                        onChat       = { navController.navigate("chat/$parkingId") },
                        onScanQr     = {
                            // Navega al escáner pasando el ID de la reserva
                            navController.navigate(
                                "${Routes.OWNER_QR_SCANNER}/${reservation.id}"
                            )
                        }
                    )
                }
            }
        }
    }
}

// ── Tarjeta de cabecera ────────────────────────────────────────────────────────
// (Sin cambios respecto a la versión anterior — se mantiene idéntica)
@Composable
fun ParkingDetailHeaderCard(
    name        : String,
    address     : String,
    reservations: Int,
    available   : Int,
    totalSpaces : Int
) {
    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text       = name,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(16.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text       = "$reservations",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text  = "Reservas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text       = "$available/$totalSpaces",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text  = "Disponibles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Estado sin reservas ────────────────────────────────────────────────────────
@Composable
fun EmptyReservationsState() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier        = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Person,
                contentDescription = null,
                modifier           = Modifier.size(44.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text       = "Sin reservas aún",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text  = "Las reservas de tus clientes\naparecerán aquí",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Tarjeta de cliente ─────────────────────────────────────────────────────────
// NUEVA: parámetro isVerified + botón onScanQr
@Composable
fun ReservationClientCard(
    userName  : String,
    plate     : String,
    hour      : String,
    isVerified: Boolean,          // ← NUEVO
    onChat    : () -> Unit,
    onScanQr  : () -> Unit        // ← NUEVO
) {
    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = if (isVerified)
                Color(0xFF4CAF50).copy(alpha = 0.06f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // ── Avatar + nombre ───────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier        = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isVerified)
                                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isVerified) Icons.Default.CheckCircle else Icons.Default.Person,
                            contentDescription = null,
                            tint               = if (isVerified)
                                Color(0xFF2E7D32)
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text       = userName,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text  = "Cliente",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Badge "Verificado" animado
                AnimatedVisibility(
                    visible = isVerified,
                    enter   = scaleIn() + fadeIn()
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint     = Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text       = "Verificado",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Chips de placa y hora ─────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier           = Modifier.size(15.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = plate,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier           = Modifier.size(15.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = hour,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Banner "Llegada confirmada" (visible solo cuando está verificado)
            AnimatedVisibility(
                visible = isVerified,
                enter   = fadeIn()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = Color(0xFF4CAF50).copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint     = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text       = "Llegada confirmada",
                                    style      = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = Color(0xFF2E7D32)
                                )
                                Text(
                                    text  = "El cliente ha sido verificado por QR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF388E3C)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Botones de acción ─────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                // Botón Chat
                OutlinedButton(
                    onClick  = onChat,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Chatear",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Botón Escanear QR
                Button(
                    onClick  = onScanQr,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape  = RoundedCornerShape(12.dp),
                    // Si ya está verificado, aparece en verde
                    colors = if (isVerified)
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor   = Color.White
                        )
                    else
                        ButtonDefaults.buttonColors()
                ) {
                    Icon(
                        if (isVerified) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isVerified) "Verificado" else "Escanear QR",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}