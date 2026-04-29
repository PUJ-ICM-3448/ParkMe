package com.example.parkme.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

data class ParkMeSensorState(
    val lightLux: Float?,
    val acceleration: Float?,
    val magneticField: Float?
) {
    val shouldUseDarkTheme: Boolean
        get() = lightLux != null && lightLux < 100f
}

@Composable
fun rememberParkMeSensorState(): ParkMeSensorState {

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

    var lightLux by remember { mutableStateOf<Float?>(null) }
    var acceleration by remember { mutableStateOf<Float?>(null) }
    var magneticField by remember { mutableStateOf<Float?>(null) }

    DisposableEffect(Unit) {

        val listener = object : SensorEventListener {

            override fun onSensorChanged(event: SensorEvent) {

                when (event.sensor.type) {

                    Sensor.TYPE_LIGHT -> {
                        lightLux = event.values[0]
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        acceleration = sqrt(x * x + y * y + z * z)
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        magneticField = sqrt(x * x + y * y + z * z)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        lightSensor?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        accelerometer?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        magnetometer?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return ParkMeSensorState(
        lightLux = lightLux,
        acceleration = acceleration,
        magneticField = magneticField
    )
}

fun getLightStatus(lux: Float?): String {
    return when {
        lux == null -> "Analizando iluminación del entorno..."
        lux < 20f -> "Zona muy oscura"
        lux < 100f -> "Iluminación baja"
        lux < 500f -> "Iluminación aceptable"
        else -> "Zona bien iluminada"
    }
}

fun getMovementStatus(acceleration: Float?): String {
    return when {
        acceleration == null -> "Analizando movimiento..."
        acceleration < 10.8f -> "Usuario estable"
        acceleration < 15f -> "Movimiento moderado"
        else -> "Movimiento fuerte detectado"
    }
}

fun getOrientationStatus(magneticField: Float?): String {
    return when {
        magneticField == null -> "Analizando orientación..."
        magneticField > 100f -> "Posible interferencia magnética"
        else -> "Orientación disponible"
    }
}

fun formatSensorValue(value: Float?): String {
    return value?.let {
        String.format("%.2f", it)
    } ?: "--"
}