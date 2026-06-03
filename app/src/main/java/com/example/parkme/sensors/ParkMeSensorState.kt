package com.example.parkme.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

/**
 * Sensores seleccionados para ParkMe:
 * 1. LIGHT         — evalúa si el parqueadero está bien iluminado (seguridad)
 * 2. ACCELEROMETER — detecta si el usuario está en movimiento (conduciendo/caminando)
 * 3. MAGNETIC_FIELD — orientación magnética del dispositivo (brújula)
 */
data class ParkMeSensorState(
    val lightLux: Float?,
    val acceleration: Float?,
    val magneticField: Float?
) {
    val shouldUseDarkTheme: Boolean
        get() = lightLux != null && lightLux < 50f

    val isUserMoving: Boolean
        get() = acceleration != null && acceleration > 12f
}

@Composable
fun rememberParkMeSensorState(): ParkMeSensorState {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val lightSensor    = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }
    val accelerometer  = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magnetometer   = remember { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    var lightLux      by remember { mutableStateOf<Float?>(null) }
    var acceleration  by remember { mutableStateOf<Float?>(null) }
    var magneticField by remember { mutableStateOf<Float?>(null) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_LIGHT -> {
                        lightLux = event.values[0]
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                        acceleration = sqrt(x * x + y * y + z * z)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                        magneticField = sqrt(x * x + y * y + z * z)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        lightSensor?.let   { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let  { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose { sensorManager.unregisterListener(listener) }
    }

    return ParkMeSensorState(lightLux = lightLux, acceleration = acceleration, magneticField = magneticField)
}

// ── Helpers de texto ──────────────────────────────────────────────────────────

fun getLightStatus(lux: Float?): String = when {
    lux == null -> "Midiendo..."
    lux < 20f   -> "Zona muy oscura "
    lux < 100f  -> "Iluminación baja "
    lux < 500f  -> "Iluminación aceptable "
    else        -> "Bien iluminado "
}

fun getMovementStatus(acceleration: Float?): String = when {
    acceleration == null  -> "Midiendo..."
    acceleration < 10.5f  -> "Estático "
    acceleration < 13f    -> "Caminando "
    else                  -> "En movimiento rápido "
}

fun getOrientationStatus(magneticField: Float?): String = when {
    magneticField == null -> "Midiendo..."
    magneticField < 25f   -> "Campo magnético débil"
    magneticField < 65f   -> "Campo magnético normal"
    magneticField < 100f  -> "Campo magnético fuerte"
    else                  -> "Interferencia magnética"
}

fun formatSensorValue(value: Float?): String =
    value?.let { String.format("%.1f", it) } ?: "--"

fun getLightAlert(lux: Float?): String = when {
    lux == null  -> "Analizando la iluminación del entorno."
    lux < 20f    -> " Zona muy oscura. Verifica el entorno antes de estacionar."
    lux < 100f   -> " Iluminación baja. Puede afectar la seguridad del lugar."
    lux < 500f   -> "Iluminación aceptable para llegar al parqueadero."
    else         -> "Excelente iluminación en la zona del parqueadero."
}

fun getMovementAlert(acceleration: Float?): String = when {
    acceleration == null -> "Analizando si el usuario está en movimiento."
    acceleration < 10.5f -> "Estás estático. Puedes consultar el parqueadero con calma."
    acceleration < 13f   -> "Estás caminando. Ten cuidado al usar la app."
    else                 -> " Movimiento rápido detectado. Evita usar la app mientras conduces."
}

fun getParkMeEnvironmentAdvice(sensorState: ParkMeSensorState): String = when {
    sensorState.lightLux != null && sensorState.lightLux < 100f ->
        " Zona oscura detectada cerca del parqueadero."
    sensorState.acceleration != null && sensorState.acceleration > 13f ->
        " Estás en movimiento. Usa la app con precaución."
    sensorState.magneticField != null && sensorState.magneticField > 100f ->
        " Interferencia magnética detectada. La brújula puede no ser precisa."
    sensorState.lightLux != null && sensorState.acceleration != null ->
        " Entorno estable para reservar."
    else -> "Leyendo sensores del entorno..."
}