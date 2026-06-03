package com.example.parkme.ui.screens

import android.Manifest
import android.os.Build
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
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.location.LocationTrackingService
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

private val BOGOTA_DEFAULT = LatLng(4.6097, -74.0817)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    navController: NavController,
    sensorState: ParkMeSensorState
) {
    val context = LocalContext.current

    var userLocation   by remember { mutableStateOf<LatLng?>(null) }
    var locationStatus by remember { mutableStateOf("Obteniendo ubicación...") }
    // Flag para saber si ya tenemos permisos y podemos arrancar el servicio
    var locationPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BOGOTA_DEFAULT, 12f)
    }

    // ── Launcher de permisos de ubicación ────────────────────────────────────
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = granted
        if (granted) {
            getCurrentLocation(context) { latLng ->
                userLocation   = latLng
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
                userLocation   = latLng
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
                update     = CameraUpdateFactory.newLatLngZoom(location, 14f),
                durationMs = 1000
            )
        }
    }

    AppDrawer(navController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ParkMe", style = MaterialTheme.typography.titleLarge) },
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
                        containerColor             = MaterialTheme.colorScheme.primary,
                        titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor     = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                ParkMeGoogleMap(
                    cameraPositionState = cameraPositionState,
                    userLocation        = userLocation,
                    onParkingClick      = { parkingId ->
                        navController.navigate("${Routes.PARKING_DETAIL}/$parkingId")
                    }
                )

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
                    modifier       = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        imageVector        = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint               = MaterialTheme.colorScheme.primary
                    )
                }

                HomeBottomSheet(
                    navController  = navController,
                    sensorState    = sensorState,
                    locationStatus = locationStatus,
                    modifier       = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun ParkMeGoogleMap(
    cameraPositionState: CameraPositionState,
    userLocation: LatLng?,
    onParkingClick: (Int) -> Unit
) {
    val parkings = MockParkingData.parkingList

    GoogleMap(
        modifier            = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties          = MapProperties(
            isMyLocationEnabled = userLocation != null,
            mapType             = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled     = false
        )
    ) {
        userLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Tú estás aquí",
                icon  = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }

        parkings.forEach { parking ->
            val position        = LatLng(parking.lat, parking.lng)
            val availableSpaces = parking.totalSpaces - parking.occupiedSpaces
            val markerColor     = when {
                availableSpaces <= 0 -> BitmapDescriptorFactory.HUE_RED
                availableSpaces <= 3 -> BitmapDescriptorFactory.HUE_ORANGE
                else                 -> BitmapDescriptorFactory.HUE_GREEN
            }
            Marker(
                state   = MarkerState(position = position),
                title   = parking.name,
                snippet = "Disponibles: $availableSpaces | $${parking.pricePerHour.toInt()}/hr",
                icon    = BitmapDescriptorFactory.defaultMarker(markerColor),
                onClick = { onParkingClick(parking.id); false }
            )
        }
    }
}

@Composable
fun HomeBottomSheet(
    navController: NavController,
    sensorState: ParkMeSensorState,
    locationStatus: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp),
        shape     = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            Box(
                modifier = Modifier
                    .width(40.dp).height(4.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(50)
                    )
            )

            Spacer(Modifier.height(12.dp))
            Text("Parqueaderos cerca", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text("📍 $locationStatus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))

            EnvironmentSensorSection(sensorState = sensorState)

            Spacer(Modifier.height(12.dp))

            Button(onClick = { navController.navigate(Routes.PARKING_LIST) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Search, null); Spacer(Modifier.width(8.dp)); Text("Buscar parqueadero")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { navController.navigate(Routes.SEARCH_ADDRESS) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Place, null); Spacer(Modifier.width(8.dp)); Text("Buscar por dirección")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { navController.navigate(Routes.MY_RESERVATIONS) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.List, null); Spacer(Modifier.width(8.dp)); Text("Ver mis reservas")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = { navController.navigate(Routes.USER_TRACKING) },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.People, null); Spacer(Modifier.width(8.dp)); Text("Ver usuarios en tiempo real")
            }
        }
    }
}

@Composable
fun EnvironmentSensorSection(sensorState: ParkMeSensorState) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Estado inteligente del entorno", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Luz: ${formatSensorValue(sensorState.lightLux)} lux - ${getLightStatus(sensorState.lightLux)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("Movimiento: ${formatSensorValue(sensorState.acceleration)} m/s² - ${getMovementStatus(sensorState.acceleration)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("Orientación: ${formatSensorValue(sensorState.magneticField)} µT - ${getOrientationStatus(sensorState.magneticField)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(getParkMeEnvironmentAdvice(sensorState), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
        else -> "ParkMe está leyendo las condiciones del entorno."
    }
}