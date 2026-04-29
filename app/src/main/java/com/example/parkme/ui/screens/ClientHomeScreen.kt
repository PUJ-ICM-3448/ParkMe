package com.example.parkme.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.location.getCurrentLocation
import com.example.parkme.location.hasLocationPermission
import com.example.parkme.navigation.Routes
import com.example.parkme.sensors.ParkMeSensorState
import com.example.parkme.sensors.formatSensorValue
import com.example.parkme.sensors.getLightStatus
import com.example.parkme.sensors.getMovementStatus
import com.example.parkme.sensors.getOrientationStatus
import com.example.parkme.ui.components.AppDrawer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// Bogotá como posición por defecto si no hay GPS
private val BOGOTA_DEFAULT = LatLng(4.6097, -74.0817)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    navController: NavController,
    sensorState: ParkMeSensorState
) {
    val context = LocalContext.current

    // ── Estado de ubicación del usuario ──────────────────────────────────────
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationStatus by remember { mutableStateOf("Obteniendo ubicación...") }

    // Posición inicial de la cámara del mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BOGOTA_DEFAULT, 12f)
    }

    // ── Launcher para pedir permisos de localización ─────────────────────────
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            getCurrentLocation(context) { latLng ->
                userLocation = latLng
                locationStatus = if (latLng != null) "Ubicación obtenida" else "No se pudo obtener ubicación"
            }
        } else {
            locationStatus = "Permiso de ubicación denegado"
        }
    }

    // ── Al entrar a la pantalla, intentar obtener ubicación ──────────────────
    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "ParkMe",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notificaciones")
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

                // ── MAPA REAL DE GOOGLE MAPS
                ParkMeGoogleMap(
                    cameraPositionState = cameraPositionState,
                    userLocation = userLocation,
                    onParkingClick = { parkingId ->
                        navController.navigate("${Routes.PARKING_DETAIL}/$parkingId")
                    }
                )

                // ── Botón de "mi ubicación"
                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission(context)) {
                            getCurrentLocation(context) { latLng ->
                                userLocation = latLng
                            }
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
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // ── Bottom Sheet con botones y sensores
                HomeBottomSheet(
                    navController = navController,
                    sensorState = sensorState,
                    locationStatus = locationStatus,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

            }
        }
    }
}

// Composable del mapa con marcadores de parqueaderos
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
            isMyLocationEnabled = userLocation != null,   // Punto azul nativo de Google Maps
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
    ) {

        // ── Marcador de ubicación del usuario
        userLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Tú estás aquí",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }

        // ── Marcadores de parqueaderos
        parkings.forEach { parking ->
            val position = LatLng(parking.lat, parking.lng)
            val availableSpaces = parking.totalSpaces - parking.occupiedSpaces
            val markerColor = when {
                availableSpaces <= 0 -> BitmapDescriptorFactory.HUE_RED      // Lleno
                availableSpaces <= 3 -> BitmapDescriptorFactory.HUE_ORANGE   // Casi lleno
                else -> BitmapDescriptorFactory.HUE_GREEN                     // Disponible
            }

            Marker(
                state = MarkerState(position = position),
                title = parking.name,
                snippet = "Disponibles: $availableSpaces | $${parking.pricePerHour.toInt()}/hr",
                icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                onClick = {
                    onParkingClick(parking.id)
                    false
                }
            )
        }
    }
}

// Bottom Sheet actualizado
@Composable
fun HomeBottomSheet(
    navController: NavController,
    sensorState: ParkMeSensorState,
    locationStatus: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // Handle visual
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(50)
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Parqueaderos cerca", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(2.dp))

            // Estado GPS en tiempo real
            Text(
                text = "📍 $locationStatus",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sensores
            EnvironmentSensorSection(sensorState = sensorState)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate(Routes.PARKING_LIST) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar parqueadero")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { navController.navigate(Routes.SEARCH_ADDRESS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar por dirección")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate(Routes.MY_RESERVATIONS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.List, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver mis reservas")
            }
        }
    }
}

// Sección de sensores
@Composable
fun EnvironmentSensorSection(sensorState: ParkMeSensorState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Text(
                text = "Estado inteligente del entorno",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Luz: ${formatSensorValue(sensorState.lightLux)} lux - ${getLightStatus(sensorState.lightLux)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Movimiento: ${formatSensorValue(sensorState.acceleration)} m/s² - ${getMovementStatus(sensorState.acceleration)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Orientación: ${formatSensorValue(sensorState.magneticField)} µT - ${getOrientationStatus(sensorState.magneticField)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getParkMeEnvironmentAdvice(sensorState),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

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
        else ->
            "ParkMe está leyendo las condiciones del entorno."
    }
}