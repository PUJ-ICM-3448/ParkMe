package com.example.parkme.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.*
import com.example.parkme.navigation.Routes
import com.example.parkme.ui.components.AppDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(navController: NavController) {

    val user = MockAuth.currentUser

    val myParkings = MockParkingData.parkingList.filter {
        it.ownerEmail == user?.email
    }

    AppDrawer(navController) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "ParkMe",
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Panel arrendador",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }
                        IconButton(onClick = { navController.navigate(Routes.ADD_PARKING) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor             = MaterialTheme.colorScheme.primary,
                        titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor     = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->

            // ── Estado vacío ──────────────────────────────────────────────────
            if (myParkings.isEmpty()) {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier        = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Default.LocalParking,
                                contentDescription = null,
                                modifier           = Modifier.size(48.dp),
                                tint               = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text       = "Sin parqueaderos aún",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text  = "Registra tu primer parqueadero\ny empieza a recibir reservas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick  = { navController.navigate(Routes.ADD_PARKING) },
                            modifier = Modifier.height(52.dp),
                            shape    = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Crear parqueadero",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                return@Scaffold
            }

            // ── Lista de parqueaderos ─────────────────────────────────────────
            LazyColumn(
                modifier            = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding      = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Resumen en cabecera
                item {
                    OwnerSummaryBanner(totalParkings = myParkings.size)
                }

                items(myParkings) { parking ->

                    val reservations   = MockReservationData.getReservationsByParking(parking.id)
                    val available      = parking.totalSpaces - parking.occupiedSpaces
                    val occupancyRatio = if (parking.totalSpaces > 0)
                        parking.occupiedSpaces.toFloat() / parking.totalSpaces else 0f

                    OwnerParkingCard(
                        parkingName    = parking.name,
                        address        = parking.address,
                        available      = available,
                        totalSpaces    = parking.totalSpaces,
                        occupancyRatio = occupancyRatio,
                        reservations   = reservations.size,
                        onCardClick    = {
                            navController.navigate("${Routes.OWNER_PARKING_DETAIL}/${parking.id}")
                        },
                        onAddSpace     = { parking.totalSpaces++ },
                        onRemoveSpace  = {
                            if (parking.totalSpaces > parking.occupiedSpaces)
                                parking.totalSpaces--
                        }
                    )
                }
            }
        }
    }
}

// ── Banner de resumen ─────────────────────────────────────────────────────────
@Composable
fun OwnerSummaryBanner(totalParkings: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text       = "Mis parqueaderos",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text       = "$totalParkings registrados",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Box(
                modifier        = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.LocalParking,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ── Tarjeta de parqueadero (arrendador) ────────────────────────────────────────
@Composable
fun OwnerParkingCard(
    parkingName    : String,
    address        : String,
    available      : Int,
    totalSpaces    : Int,
    occupancyRatio : Float,
    reservations   : Int,
    onCardClick    : () -> Unit,
    onAddSpace     : () -> Unit,
    onRemoveSpace  : () -> Unit
) {
    val occupancyColor = when {
        occupancyRatio >= 1f   -> MaterialTheme.colorScheme.error
        occupancyRatio >= 0.8f -> MaterialTheme.colorScheme.tertiary
        else                   -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier  = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onCardClick() },
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Nombre + flecha
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = parkingName,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector        = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            // Barra de ocupación
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "Disponibles: $available / $totalSpaces",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = occupancyColor
                )
                Text(
                    text  = "${(occupancyRatio * 100).toInt()}% ocupado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress  = { occupancyRatio },
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color      = occupancyColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(16.dp))

            // Controles de cupos
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text  = "Cupos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onAddSpace,
                    shape   = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onRemoveSpace,
                    shape   = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Quitar", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))

            // Reservas + hint
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier        = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "$reservations reservas activas",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text  = "Ver clientes →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}