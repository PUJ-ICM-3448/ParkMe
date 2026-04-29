package com.example.parkme.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.location.getCurrentLocation
import com.example.parkme.location.hasLocationPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

private const val DIRECTIONS_API_KEY = "AIzaSyBpxMlFDoFFac28_SGTlq17ctqSBTKt_aQ"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    navController: NavController,
    destLat: Double,
    destLng: Double,
    parkingName: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val destination = LatLng(destLat, destLng)

    var userLocation  by remember { mutableStateOf<LatLng?>(null) }
    var routePoints   by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var statusMessage by remember { mutableStateOf("Obteniendo tu ubicación...") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destination, 14f)
    }

    // ── Llama a Directions API y dibuja la polyline ───────────────────────────
    fun fetchRoute(origin: LatLng) {
        coroutineScope.launch {
            statusMessage = "Calculando ruta..."
            try {
                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&mode=driving" +
                        "&key=$DIRECTIONS_API_KEY"

                val response = withContext(Dispatchers.IO) { URL(url).readText() }
                val json     = JSONObject(response)
                val status   = json.getString("status")

                if (status == "OK") {
                    val encoded = json
                        .getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")

                    routePoints = decodePolyline(encoded)

                    if (routePoints.isNotEmpty()) {
                        val boundsBuilder = LatLngBounds.Builder()
                        routePoints.forEach { boundsBuilder.include(it) }
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngBounds(
                                boundsBuilder.build(), 120
                            ),
                            durationMs = 1200
                        )
                        statusMessage = "Ruta calculada "
                    }
                } else {
                    // Si la Directions API no está habilitada, avisar con el botón fallback
                    statusMessage = "Ruta no disponible en app "
                }
            } catch (e: Exception) {
                statusMessage = "Sin conexión."
            }
        }
    }


    // ── Launcher de permisos ──────────────────────────────────────────────────
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            getCurrentLocation(context) { latLng ->
                userLocation = latLng
                if (latLng != null) fetchRoute(latLng)
                else statusMessage = "No se pudo obtener tu ubicación"
            }
        } else {
            statusMessage = "Permiso de ubicación denegado"
        }
    }

    // Al entrar, obtener ubicación y calcular ruta
    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            getCurrentLocation(context) { latLng ->
                userLocation = latLng
                if (latLng != null) fetchRoute(latLng)
                else statusMessage = "No se pudo obtener tu ubicación"
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

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ruta a $parkingName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Mapa
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
                // Marcador: usuario
                userLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = loc),
                        title = "Tu ubicación",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
                }

                // Marcador: parqueadero destino
                Marker(
                    state   = MarkerState(position = destination),
                    title   = parkingName,
                    snippet = "Destino"
                )

                // Polyline de la ruta (si Directions API respondió OK)
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color  = Color(0xFF1565C0),
                        width  = 14f
                    )
                }
            }

            // FAB: recentrar / recalcular
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission(context)) {
                        getCurrentLocation(context) { latLng ->
                            userLocation = latLng
                            if (latLng != null) fetchRoute(latLng)
                        }
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

            // Panel inferior: estado + botón fallback
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text  = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Decodifica Encoded Polyline de Google en lista de LatLng
// ─────────────────────────────────────────────────────────────────────────────
fun decodePolyline(encoded: String): List<LatLng> {
    val poly  = mutableListOf<LatLng>()
    var index = 0
    val len   = encoded.length
    var lat   = 0
    var lng   = 0

    while (index < len) {
        var b: Int; var shift = 0; var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        lat += if ((result and 1) != 0) (result shr 1).inv() else result shr 1

        shift = 0; result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        lng += if ((result and 1) != 0) (result shr 1).inv() else result shr 1

        poly.add(LatLng(lat / 1E5, lng / 1E5))
    }
    return poly
}