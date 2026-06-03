package com.example.parkme.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.model.UserLocation
import com.example.parkme.location.LocationRepository
import com.example.parkme.location.getCurrentLocation
import com.example.parkme.location.hasLocationPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.flow.collect

// Colores para los marcadores de otros usuarios
private val USER_MARKER_COLORS = listOf(
    BitmapDescriptorFactory.HUE_RED,
    BitmapDescriptorFactory.HUE_ORANGE,
    BitmapDescriptorFactory.HUE_MAGENTA,
    BitmapDescriptorFactory.HUE_ROSE,
    BitmapDescriptorFactory.HUE_VIOLET,
    BitmapDescriptorFactory.HUE_YELLOW,
)

private val BOGOTA_DEFAULT = LatLng(4.6097, -74.0817)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTrackingMapScreen(navController: NavController) {

    val context = LocalContext.current
    val currentUser = MockAuth.currentUser

    // ── Estado ────────────────────────────────────────────────────────────────
    var myLocation       by remember { mutableStateOf<LatLng?>(null) }
    var activeUsers      by remember { mutableStateOf<List<UserLocation>>(emptyList()) }
    var showUserList     by remember { mutableStateOf(false) }
    var selectedUser     by remember { mutableStateOf<UserLocation?>(null) }
    var permissionStatus by remember { mutableStateOf("Obteniendo ubicación...") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BOGOTA_DEFAULT, 13f)
    }

    // ── Launcher de permisos ──────────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            getCurrentLocation(context) { latLng ->
                myLocation = latLng
                latLng?.let {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 14f))
                }
            }
            permissionStatus = "Ubicación activa"
        } else {
            permissionStatus = "Permiso denegado"
        }
    }

    // ── Al entrar: obtener mi ubicación ──────────────────────────────────────
    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            getCurrentLocation(context) { latLng ->
                myLocation = latLng
                latLng?.let {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 14f))
                }
                permissionStatus = if (latLng != null) "Ubicación activa" else "Sin ubicación"
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ── Observar usuarios activos desde Firebase ──────────────────────────────
    LaunchedEffect(currentUser) {
        val myEmail = currentUser?.email ?: return@LaunchedEffect
        LocationRepository.observeActiveUsers(myEmail).collect { users ->
            activeUsers = users
        }
    }

    // ── Centrar cámara al seleccionar un usuario ──────────────────────────────
    LaunchedEffect(selectedUser) {
        selectedUser?.let { user ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(user.latitude, user.longitude), 15f
                ),
                durationMs = 800
            )
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seguimiento de usuarios", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${activeUsers.size} usuario(s) activo(s)",
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
                actions = {
                    // Botón para abrir/cerrar lista de usuarios
                    IconButton(onClick = { showUserList = !showUserList }) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Ver usuarios",
                            tint = if (activeUsers.isNotEmpty())
                                Color(0xFFFFEB3B)
                            else
                                MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor        = MaterialTheme.colorScheme.primary,
                    titleContentColor     = MaterialTheme.colorScheme.onPrimary,
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

            // ── MAPA ─────────────────────────────────────────────────────────
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = myLocation != null,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled     = false
                )
            ) {

                // Marcador: mi ubicación
                myLocation?.let { loc ->
                    Marker(
                        state   = MarkerState(position = loc),
                        title   = "Yo (${currentUser?.name ?: ""})",
                        snippet = "Mi ubicación actual",
                        icon    = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
                }

                // Marcadores: otros usuarios activos
                activeUsers.forEachIndexed { index, user ->
                    val colorHue = USER_MARKER_COLORS[index % USER_MARKER_COLORS.size]
                    Marker(
                        state   = MarkerState(position = LatLng(user.latitude, user.longitude)),
                        title   = user.name,
                        snippet = user.email,
                        icon    = BitmapDescriptorFactory.defaultMarker(colorHue),
                        onClick = {
                            selectedUser = user
                            false  // false = mostrar info window
                        }
                    )
                }
            }

            // ── FAB: mi ubicación ─────────────────────────────────────────────
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission(context)) {
                        getCurrentLocation(context) { latLng ->
                            myLocation = latLng
                            latLng?.let {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngZoom(it, 14f)
                                )
                            }
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

            // ── Badge de estado ───────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape  = RoundedCornerShape(20.dp),
                color  = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (activeUsers.isNotEmpty()) Color(0xFF4CAF50)
                                else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = permissionStatus,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // ── Panel lateral de lista de usuarios ────────────────────────────
            if (showUserList) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(200.dp)
                        .padding(end = 8.dp, top = 56.dp, bottom = 8.dp),
                    shape   = RoundedCornerShape(12.dp),
                    color   = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        Text(
                            text  = "Usuarios activos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        if (activeUsers.isEmpty()) {
                            Text(
                                text  = "Ningún otro usuario activo ahora",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(activeUsers) { user ->
                                    UserListItem(
                                        user      = user,
                                        colorHue  = USER_MARKER_COLORS[
                                            activeUsers.indexOf(user) % USER_MARKER_COLORS.size
                                        ],
                                        onClick   = {
                                            selectedUser = user
                                            showUserList = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Info card del usuario seleccionado ────────────────────────────
            selectedUser?.let { user ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonPin,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text  = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = "📍 ${String.format("%.5f", user.latitude)}, " +
                                        "${String.format("%.5f", user.longitude)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { selectedUser = null }) {
                            Text("✕", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(
    user: UserLocation,
    colorHue: Float,
    onClick: () -> Unit
) {
    TextButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = hueToColor(colorHue),
                        shape = CircleShape
                    )
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text  = user.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = user.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Convierte un HUE de BitmapDescriptorFactory en un Color de Compose */
private fun hueToColor(hue: Float): Color = when {
    hue < 30f  -> Color(0xFFFF5722)  // rojo-naranja
    hue < 60f  -> Color(0xFFFF9800)  // naranja
    hue < 90f  -> Color(0xFFFFEB3B)  // amarillo
    hue < 150f -> Color(0xFF4CAF50)  // verde
    hue < 210f -> Color(0xFF03A9F4)  // celeste
    hue < 270f -> Color(0xFF9C27B0)  // violeta
    hue < 330f -> Color(0xFFE91E63)  // rosa
    else       -> Color(0xFFF44336)  // rojo
}