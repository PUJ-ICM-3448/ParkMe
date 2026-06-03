package com.example.parkme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.parkme.navigation.AppNavigation
import com.example.parkme.notifications.ParkMeNotificationHelper
import com.example.parkme.sensors.rememberParkMeSensorState
import com.example.parkme.ui.theme.ParkMeTheme

class MainActivity : ComponentActivity() {

    // Launcher para solicitar el permiso POST_NOTIFICATIONS en Android 13+
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* resultado ignorado */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear canales de notificación al iniciar la app
        ParkMeNotificationHelper.createChannels(this)

        // Solicitar permiso de notificaciones en Android 13+ (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            val sensorState = rememberParkMeSensorState()
            ParkMeTheme(darkTheme = sensorState.shouldUseDarkTheme) {
                AppNavigation(sensorState = sensorState)
            }
        }
    }
}
