package com.example.parkme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.parkme.navigation.AppNavigation
import com.example.parkme.sensors.rememberParkMeSensorState
import com.example.parkme.ui.theme.ParkMeTheme
// ✅ NUEVO: imports para forzar renderer legacy
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ NUEVO: Forzar renderer legacy ANTES de cualquier otra cosa
        // Soluciona el problema de mapa en blanco en dispositivos Transsion/Infinix
        MapsInitializer.initialize(applicationContext, Renderer.LEGACY) { result ->
            // result indica qué renderer quedó activo (LEGACY o LATEST)
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            val sensorState = rememberParkMeSensorState()

            ParkMeTheme(
                darkTheme = sensorState.shouldUseDarkTheme
            ) {

                AppNavigation(sensorState)

            }

        }

    }

}