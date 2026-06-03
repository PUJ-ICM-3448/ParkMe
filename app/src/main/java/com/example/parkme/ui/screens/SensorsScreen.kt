package com.example.parkme.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.parkme.sensors.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(navController: NavController) {

    val context = LocalContext.current

    // Usar el mismo estado de sensores centralizado (solo luz + acelerómetro)
    val sensorState = rememberParkMeSensorState()

    var cameraImage   by remember { mutableStateOf<Bitmap?>(null) }
    var cameraMessage by remember { mutableStateOf("Toma una foto del acceso o fachada del parqueadero.") }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        cameraImage   = bitmap
        cameraMessage = if (bitmap != null) "✅ Foto capturada correctamente." else "No se capturó ninguna foto."
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else cameraMessage = "Permiso de cámara denegado."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensores ParkMe") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text = "Evaluación inteligente del entorno",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "ParkMe usa el sensor de luz y el acelerómetro para evaluar las condiciones del entorno antes de que llegues al parqueadero.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Alerta general del entorno ──────────────────────────────────
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = getParkMeEnvironmentAdvice(sensorState),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Sensor 1: Luz ───────────────────────────────────────────────
            SensorCard(
                title       = "🔆 Sensor de Luz",
                value       = "${formatSensorValue(sensorState.lightLux)} lux — ${getLightStatus(sensorState.lightLux)}",
                description = getLightAlert(sensorState.lightLux),
                icon        = Icons.Default.LightMode
            )

            // ── Sensor 2: Acelerómetro ──────────────────────────────────────
            SensorCard(
                title       = "📳 Acelerómetro",
                value       = "${formatSensorValue(sensorState.acceleration)} m/s² — ${getMovementStatus(sensorState.acceleration)}",
                description = getMovementAlert(sensorState.acceleration),
                icon        = Icons.Default.Speed
            )

            // ── Cámara ──────────────────────────────────────────────────────
            SensorCard(
                title       = "📷 Cámara",
                value       = cameraMessage,
                description = "Toma evidencia visual del parqueadero: fachada, acceso, estado de cupos.",
                icon        = Icons.Default.CameraAlt
            )

            Button(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) cameraLauncher.launch(null)
                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Tomar foto del parqueadero")
            }

            cameraImage?.let { bitmap ->
                Image(
                    bitmap             = bitmap.asImageBitmap(),
                    contentDescription = "Foto del parqueadero",
                    modifier           = Modifier.fillMaxWidth().height(220.dp)
                )
            }
        }
    }
}

@Composable
private fun SensorCard(
    title: String,
    value: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
