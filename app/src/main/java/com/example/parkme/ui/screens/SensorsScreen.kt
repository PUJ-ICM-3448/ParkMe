package com.example.parkme.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(navController: NavController) {

    val context = LocalContext.current

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val lightSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    val magnetometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    var lightValue by remember { mutableStateOf<Float?>(null) }
    var accelerationValue by remember { mutableStateOf<Float?>(null) }
    var magneticValue by remember { mutableStateOf<Float?>(null) }

    var cameraImage by remember { mutableStateOf<Bitmap?>(null) }
    var cameraMessage by remember {
        mutableStateOf("Toma una foto del acceso, fachada o zona del parqueadero.")
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            cameraImage = bitmap
            cameraMessage = "Foto capturada correctamente para evidencia del parqueadero."
        } else {
            cameraMessage = "No se capturó ninguna foto."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            cameraMessage = "Permiso de cámara denegado. Actívalo para tomar evidencia visual."
        }
    }

    DisposableEffect(Unit) {

        val listener = object : SensorEventListener {

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {

                    Sensor.TYPE_LIGHT -> {
                        lightValue = event.values[0]
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        accelerationValue = sqrt(x * x + y * y + z * z)
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        magneticValue = sqrt(x * x + y * y + z * z)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        lightSensor?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        accelerometer?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        magnetometer?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensores ParkMe") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text = "Evaluación del entorno del parqueadero",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Esta pantalla usa cámara, luz, acelerómetro y magnetómetro para apoyar una evaluación básica del lugar antes de reservar o llegar al parqueadero.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            EnvironmentAlertCard(
                message = getEnvironmentStatus(
                    lux = lightValue,
                    acceleration = accelerationValue,
                    magneticValue = magneticValue
                )
            )

            SensorInfoCard(
                title = "Cámara",
                value = cameraMessage,
                description = "Permite tomar una foto del parqueadero como evidencia visual de entrada, fachada, cupos o condiciones del lugar.",
                iconType = "camera"
            )

            Button(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        cameraLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Tomar foto del parqueadero")
            }

            cameraImage?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto tomada con la cámara",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            SensorInfoCard(
                title = "Sensor de luz",
                value = if (lightSensor == null) {
                    "No disponible en este dispositivo"
                } else {
                    "${lightValue?.toInt() ?: 0} lux - ${getLightRecommendation(lightValue)}"
                },
                description = getLightAlert(lightValue),
                iconType = "light"
            )

            SensorInfoCard(
                title = "Acelerómetro",
                value = if (accelerometer == null) {
                    "No disponible en este dispositivo"
                } else {
                    "${formatSensorValue(accelerationValue)} m/s² - ${getMovementStatus(accelerationValue)}"
                },
                description = getMovementAlert(accelerationValue),
                iconType = "movement"
            )

            SensorInfoCard(
                title = "Magnetómetro / brújula",
                value = if (magnetometer == null) {
                    "No disponible en este dispositivo"
                } else {
                    "${formatSensorValue(magneticValue)} µT"
                },
                description = getMagneticAlert(magneticValue),
                iconType = "orientation"
            )
        }
    }
}

@Composable
fun SensorInfoCard(
    title: String,
    value: String,
    description: String,
    iconType: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            RowWithIcon(
                title = title,
                iconType = iconType
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RowWithIcon(
    title: String,
    iconType: String
) {
    val icon = when (iconType) {
        "camera" -> Icons.Default.CameraAlt
        "light" -> Icons.Default.LightMode
        "movement" -> Icons.Default.Speed
        else -> Icons.Default.MyLocation
    }

    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EnvironmentAlertCard(
    message: String
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun getLightRecommendation(lux: Float?): String {
    return when {
        lux == null -> "midiendo iluminación"
        lux < 20f -> "zona muy oscura"
        lux < 100f -> "iluminación baja"
        lux < 500f -> "iluminación aceptable"
        else -> "zona bien iluminada"
    }
}

fun getLightAlert(lux: Float?): String {
    return when {
        lux == null -> "Analizando la iluminación del entorno del parqueadero."
        lux < 20f -> "Aviso ParkMe: la zona está muy oscura. Se recomienda verificar el entorno antes de estacionar."
        lux < 100f -> "Aviso ParkMe: la iluminación es baja. Puede ser necesario revisar mejor la seguridad del lugar."
        lux < 500f -> "La iluminación parece aceptable para identificar el parqueadero."
        else -> "La zona tiene buena iluminación para llegar o estacionar."
    }
}

fun getMovementStatus(acceleration: Float?): String {
    return when {
        acceleration == null -> "midiendo movimiento"
        acceleration < 10.8f -> "usuario estable"
        acceleration < 15f -> "movimiento moderado"
        else -> "movimiento fuerte"
    }
}

fun getMovementAlert(acceleration: Float?): String {
    return when {
        acceleration == null -> "Analizando si el usuario está quieto o en movimiento."
        acceleration < 10.8f -> "Usuario estable. Puede consultar la información del parqueadero con seguridad."
        acceleration < 15f -> "Movimiento detectado. Se recomienda tener cuidado al usar la app mientras camina."
        else -> "Aviso ParkMe: movimiento fuerte detectado. Evita manipular la app mientras te desplazas."
    }
}

fun getMagneticAlert(magneticValue: Float?): String {
    return when {
        magneticValue == null -> "Analizando orientación del dispositivo."
        magneticValue > 100f -> "Aviso ParkMe: posible interferencia magnética. La orientación puede ser menos precisa."
        else -> "Orientación disponible para apoyar la navegación hacia el parqueadero."
    }
}

fun getEnvironmentStatus(
    lux: Float?,
    acceleration: Float?,
    magneticValue: Float?
): String {
    return when {
        lux != null && lux < 100f -> {
            "Precaución: la iluminación del entorno es baja."
        }

        acceleration != null && acceleration > 15f -> {
            "Precaución: el usuario está en movimiento fuerte."
        }

        magneticValue != null && magneticValue > 100f -> {
            "Precaución: posible interferencia en la orientación."
        }

        lux != null && acceleration != null && magneticValue != null -> {
            "Entorno estable para consultar información del parqueadero."
        }

        else -> {
            "Analizando datos del entorno con sensores."
        }
    }
}

fun formatSensorValue(value: Float?): String {
    return value?.let {
        String.format("%.2f", it)
    } ?: "0.00"
}