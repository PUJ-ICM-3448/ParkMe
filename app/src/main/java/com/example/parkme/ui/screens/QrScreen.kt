package com.example.parkme.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.model.Reservation
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Pantalla que genera un QR con los datos de la reserva.
 * Uso de servicio externo: biblioteca ZXing (Google) para generación de códigos QR.
 * En producción, este QR sería escaneado en la entrada del parqueadero.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    navController: NavController,
    reservation: Reservation
) {
    val currentUser = MockAuth.currentUser

    // ── Generar QR con los datos de la reserva ────────────────────────────────
    val qrContent = buildString {
        append("PARKME-RESERVA\n")
        append("ID: ${reservation.id}\n")
        append("Parqueadero: ${reservation.parkingName}\n")
        append("Usuario: ${currentUser?.name ?: ""}\n")
        append("Email: ${currentUser?.email ?: ""}\n")
        append("Fecha: ${reservation.date}\n")
        append("Horas: ${reservation.hours}\n")
        append("Total: $${reservation.totalPrice}")
    }

    val qrBitmap: Bitmap? = remember(qrContent) {
        try {
            val writer = QRCodeWriter()
            val matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512)
            val bmp    = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
            for (x in 0 until 512) {
                for (y in 0 until 512) {
                    bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) { null }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR de Reserva") },
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
            modifier            = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text("Presenta este QR en la entrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Código QR generado
            if (qrBitmap != null) {
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Image(
                        bitmap             = qrBitmap.asImageBitmap(),
                        contentDescription = "Código QR de reserva",
                        modifier           = Modifier.size(260.dp).padding(12.dp)
                    )
                }
            } else {
                Text("No se pudo generar el QR", color = MaterialTheme.colorScheme.error)
            }

            // Detalles de la reserva
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReservationDetailRow("Parqueadero", reservation.parkingName)
                    ReservationDetailRow("Fecha",       reservation.date)
                    ReservationDetailRow("Duración",    "${reservation.hours} hora(s)")
                    ReservationDetailRow("Total",       "$${reservation.totalPrice} COP")
                    ReservationDetailRow("Estado",      reservation.status)
                }
            }

            Text(
                text  = "Generado con ZXing · Solo válido para esta reserva",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReservationDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}