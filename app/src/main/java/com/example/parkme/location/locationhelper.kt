package com.example.parkme.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * Verifica si la app tiene permisos de ubicación concedidos.
 */
fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Obtiene la ubicación actual del dispositivo una sola vez.
 * Llama a [onLocation] con las coordenadas o null si falla.
 */
@SuppressLint("MissingPermission")
fun getCurrentLocation(
    context: Context,
    onLocation: (LatLng?) -> Unit
) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationToken = CancellationTokenSource()

    fusedClient
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocation(LatLng(location.latitude, location.longitude))
            } else {
                // Fallback: última ubicación conocida
                fusedClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) {
                        onLocation(LatLng(lastLocation.latitude, lastLocation.longitude))
                    } else {
                        onLocation(null)
                    }
                }
            }
        }
        .addOnFailureListener {
            onLocation(null)
        }
}