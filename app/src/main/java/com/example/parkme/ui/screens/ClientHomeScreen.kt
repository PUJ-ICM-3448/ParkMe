package com.example.parkme.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllOut
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.location.LocationTrackingService
import com.example.parkme.location.getCurrentLocation
import com.example.parkme.location.hasLocationPermission
import com.example.parkme.navigation.Routes
import com.example.parkme.sensors.ParkMeSensorState
import com.example.parkme.sensors.getLightStatus
import com.example.parkme.sensors.getMovementStatus
import com.example.parkme.sensors.getOrientationStatus
import com.example.parkme.ui.components.AppDrawer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.material3.ElevatedCard

private val BOGOTA_DEFAULT = LatLng(4.6097, -74.0817)

// ── Peek height: cuánto del sheet es visible cuando está minimizado ──────────
private val PEEK_HEIGHT = 100.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    navController: NavController,
    sensorState: ParkMeSensorState
) {
    val context = LocalContext.current

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationStatus by remember { mutableStateOf("Obteniendo ubicación...") }
    var locationPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BOGOTA_DEFAULT, 12f)
    }

    // ── Scaffold state para el Bottom Sheet real ─────────────────────────────
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = granted
        if (granted) {
            getCurrentLocation(context) { latLng ->
                userLocation = latLng
                locationStatus = if (latLng != null) "Ubicación obtenida" else "No se pudo obtener ubicación"
            }
        } else {
            locationStatus = "Permiso de ubicación denegado"
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) return@LaunchedEffect
        val user = MockAuth.currentUser ?: return@LaunchedEffect
        kotlinx.coroutines.delay(500L)
        LocationTrackingService.start(context, user.email, user.name)
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            locationPermissionGranted = true
            getCurrentLocation(context) { latLng ->
                userLocation = latLng
                locationStatus = if (latLng != null) "Ubicación obtenida" else "No se pudo obtener ubicación"
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 14f),
                durationMs = 1000
            )
        }
    }

    AppDrawer(navController) {
        BottomSheetScaffold(
            scaffoldState = sheetState,
            // ── Peek: altura visible cuando está parcialmente expandido ──────
            sheetPeekHeight = PEEK_HEIGHT,
            sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            sheetShadowElevation = 16.dp,
            sheetTonalElevation = 4.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetDragHandle = {
                // Handle personalizado más visible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )
                }
            },
            sheetContent = {
                BottomSheetContent(
                    navController = navController,
                    sensorState = sensorState,
                    locationStatus = locationStatus
                )
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "ParkMe",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                            Icon(Icons.Default.Menu, "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                            Icon(Icons.Default.Notifications, "Notificaciones")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ── Mapa ocupa toda la pantalla ──────────────────────────────
                ParkMeGoogleMap(
                    cameraPositionState = cameraPositionState,
                    userLocation = userLocation,
                    onParkingClick = { parkingId ->
                        navController.navigate("${Routes.PARKING_DETAIL}/$parkingId")
                    }
                )

                // ── FAB de ubicación (esquina superior derecha) ──────────────
                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission(context)) {
                            getCurrentLocation(context) { latLng -> userLocation = latLng }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ── Contenido del Bottom Sheet ────────────────────────────────────────────────
@Composable
fun BottomSheetContent(
    navController: NavController,
    sensorState: ParkMeSensorState,
    locationStatus: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // ── Header con título y estado de ubicación ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Parqueaderos cerca",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (locationStatus == "Ubicación obtenida")
                                    Color(0xFF4CAF50)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = locationStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(20.dp))

        // ── Sección de sensores rediseñada ───────────────────────────────────
        EnvironmentSensorSection(sensorState)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(20.dp))

        // ── Accesos rápidos ──────────────────────────────────────────────────
        Text(
            text = "Accesos rápidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            HomeActionCard(
                title = "Buscar",
                icon = Icons.Default.Search,
                subtitle = "Parqueaderos",
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.PARKING_LIST) }

            HomeActionCard(
                title = "Dirección",
                icon = Icons.Default.Place,
                subtitle = "Por dirección",
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.SEARCH_ADDRESS) }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            HomeActionCard(
                title = "Reservas",
                icon = Icons.Default.List,
                subtitle = "Mis reservas",
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.MY_RESERVATIONS) }

            HomeActionCard(
                title = "Usuarios",
                icon = Icons.Default.People,
                subtitle = "En tiempo real",
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.USER_TRACKING) }
        }
    }
}

// ── Mapa (sin cambios en lógica) ─────────────────────────────────────────────
@Composable
fun ParkMeGoogleMap(
    cameraPositionState: CameraPositionState,
    userLocation: LatLng?,
    onParkingClick: (Int) -> Unit
) {
    val parkings = MockParkingData.parkingList

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = userLocation != null,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
    ) {
        userLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Tú estás aquí",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }

        parkings.forEach { parking ->
            val position = LatLng(parking.lat, parking.lng)
            val availableSpaces = parking.totalSpaces - parking.occupiedSpaces
            val markerColor = when {
                availableSpaces <= 0 -> BitmapDescriptorFactory.HUE_RED
                availableSpaces <= 3 -> BitmapDescriptorFactory.HUE_ORANGE
                else -> BitmapDescriptorFactory.HUE_GREEN
            }
            Marker(
                state = MarkerState(position = position),
                title = parking.name,
                snippet = "Disponibles: $availableSpaces | $${parking.pricePerHour.toInt()}/hr",
                icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                onClick = { onParkingClick(parking.id); false }
            )
        }
    }
}

// ── Sección de sensores como dashboard ───────────────────────────────────────
@Composable
fun EnvironmentSensorSection(
    sensorState: ParkMeSensorState
) {
    Column {
        Text(
            text = "Estado del entorno",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SensorCard(
                title = "Luz",
                icon = Icons.Default.AllOut,
                value = getLightStatus(sensorState.lightLux),
                rawValue = sensorState.lightLux?.let { "${it.toInt()} lx" } ?: "—",
                progress = ((sensorState.lightLux ?: 0f) / 1000f).coerceIn(0f, 1f),
                statusColor = when {
                    sensorState.lightLux == null -> Color(0xFF9E9E9E)
                    sensorState.lightLux < 100f -> Color(0xFFFF7043)
                    sensorState.lightLux < 400f -> Color(0xFFFFA726)
                    else -> Color(0xFF66BB6A)
                },
                modifier = Modifier.weight(1f)
            )

            SensorCard(
                title = "Movimiento",
                icon = Icons.Default.DirectionsCar,
                value = getMovementStatus(sensorState.acceleration),
                rawValue = sensorState.acceleration?.let { "${"%.1f".format(it)} m/s²" } ?: "—",
                progress = ((sensorState.acceleration ?: 0f) / 20f).coerceIn(0f, 1f),
                statusColor = when {
                    sensorState.acceleration == null -> Color(0xFF9E9E9E)
                    sensorState.acceleration > 15f -> Color(0xFFFF7043)
                    sensorState.acceleration > 8f -> Color(0xFFFFA726)
                    else -> Color(0xFF66BB6A)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        SensorCard(
            title = "Orientación / Brújula",
            icon = Icons.Default.Explore,
            value = getOrientationStatus(sensorState.magneticField),
            rawValue = sensorState.magneticField?.let { "${"%.1f".format(it)} µT" } ?: "—",
            progress = ((sensorState.magneticField ?: 0f) / 150f).coerceIn(0f, 1f),
            statusColor = when {
                sensorState.magneticField == null -> Color(0xFF9E9E9E)
                sensorState.magneticField > 100f -> Color(0xFFFFA726)
                else -> Color(0xFF66BB6A)
            },
            modifier = Modifier.fillMaxWidth(),
            horizontal = true
        )

        Spacer(Modifier.height(10.dp))

        // Chip de consejo del entorno
        val advice = getParkMeEnvironmentAdvice(sensorState)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = advice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ── Tarjeta individual de sensor ─────────────────────────────────────────────
@Composable
fun SensorCard(
    title: String,
    icon: ImageVector,
    value: String,
    rawValue: String,
    progress: Float,
    statusColor: Color,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "sensor_progress"
    )
    val animatedColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = tween(durationMillis = 400),
        label = "sensor_color"
    )

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        if (horizontal) {
            // Layout horizontal para el sensor de orientación (ancho completo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(animatedColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = animatedColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = rawValue,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = animatedColor
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = animatedColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            // Layout vertical para sensores en fila (Luz y Movimiento)
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(animatedColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = animatedColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = rawValue,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = animatedColor
                )

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = animatedColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ── Tarjeta de acción rápida ──────────────────────────────────────────────────
@Composable
fun HomeActionCard(
    title: String,
    icon: ImageVector,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Acento de color en esquina superior derecha
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Función de consejo del entorno (sin cambios) ──────────────────────────────
fun getParkMeEnvironmentAdvice(sensorState: ParkMeSensorState): String {
    return when {
        sensorState.lightLux != null && sensorState.lightLux < 100f ->
            "Aviso ParkMe: la iluminación es baja. Revisa el entorno antes de estacionar."
        sensorState.acceleration != null && sensorState.acceleration > 15f ->
            "Aviso ParkMe: movimiento fuerte detectado. Evita manipular la app mientras te desplazas."
        sensorState.magneticField != null && sensorState.magneticField > 100f ->
            "Aviso ParkMe: posible interferencia en la orientación del dispositivo."
        sensorState.lightLux != null && sensorState.acceleration != null && sensorState.magneticField != null ->
            "Entorno estable para buscar o reservar parqueadero."
        else -> "ParkMe está leyendo las condiciones del entorno."
    }
}