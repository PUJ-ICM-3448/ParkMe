package com.example.parkme.location

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.parkme.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import android.Manifest

class LocationTrackingService : Service() {

    companion object {
        private const val CHANNEL_ID  = "parkme_tracking"
        private const val NOTIF_ID    = 1001
        private const val INTERVAL_MS = 5_000L
        private const val FASTEST_MS  = 3_000L

        private const val EXTRA_EMAIL = "extra_email"
        private const val EXTRA_NAME  = "extra_name"

        fun start(context: Context, email: String, name: String) {
            val fineGranted   = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!fineGranted && !coarseGranted) return

            try {
                val intent = Intent(context, LocationTrackingService::class.java).apply {
                    putExtra(EXTRA_EMAIL, email)
                    putExtra(EXTRA_NAME,  name)
                }
                // startForegroundService requiere que la app esté en foreground visible.
                // Si la app acaba de abrirse (transición), usamos startService normal
                // y el propio onStartCommand intentará promover a foreground.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, LocationTrackingService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private lateinit var fusedClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private var userEmail = ""
    private var userName  = ""

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        userEmail = intent?.getStringExtra(EXTRA_EMAIL) ?: ""
        userName  = intent?.getStringExtra(EXTRA_NAME)  ?: ""

        // En Android 14+ (API 34+) startForeground con tipo location puede lanzar
        // ForegroundServiceStartNotAllowedException si la app no está en foreground visible.
        // Lo capturamos para evitar el crash; el tracking simplemente no se activará en ese caso.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // API 34+ : usar ServiceCompat con foregroundServiceType explícito
                ServiceCompat.startForeground(
                    this,
                    NOTIF_ID,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIF_ID, buildNotification())
            }
            startLocationUpdates()
        } catch (e: Exception) {
            // Incluye ForegroundServiceStartNotAllowedException en API 31+
            e.printStackTrace()
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            stopSelf()
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(FASTEST_MS)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                if (userEmail.isNotEmpty()) {
                    try {
                        LocationRepository.publishMyLocation(
                            email  = userEmail,
                            name   = userName,
                            latLng = LatLng(location.latitude, location.longitude)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        fusedClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationCallback?.let { fusedClient.removeLocationUpdates(it) }
            if (userEmail.isNotEmpty()) {
                LocationRepository.markInactive(userEmail)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ParkMe - Seguimiento activo",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "ParkMe está compartiendo tu ubicación"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ParkMe activo")
            .setContentText("Compartiendo ubicación con otros usuarios")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
}