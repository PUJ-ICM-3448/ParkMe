package com.example.parkme.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.camera.core.ExperimentalGetImage

// ── Pantalla de escaneo QR para el propietario ────────────────────────────────
// Recibe el reservationId del cliente que se va a verificar.
// Cuando el QR se lee, navega de vuelta pasando el resultado como argumento.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerQrScannerScreen(
    navController : NavController,
    reservationId : Int          // ID de la reserva que se espera verificar
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado de permisos
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    // Estado del escaneo
    var scanResult   by remember { mutableStateOf<String?>(null) }
    var scanSuccess  by remember { mutableStateOf(false) }
    var showDialog   by remember { mutableStateOf(false) }

    // Evitar múltiples lecturas del mismo frame
    var alreadyScanned by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ── Diálogo de verificación exitosa ──────────────────────────────────────
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint     = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Usuario verificado",
                    fontWeight  = FontWeight.Bold,
                    textAlign   = TextAlign.Center,
                    modifier    = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "La llegada del cliente ha sido confirmada correctamente.",
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        // Navega de vuelta indicando que la verificación fue exitosa.
                        // La pantalla anterior lee el argumento "verified=<reservationId>"
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("verified_reservation_id", reservationId)
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Verificar llegada",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Apunta la cámara al QR del cliente",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->

        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {

            if (hasCameraPermission) {
                // ── Vista de cámara ───────────────────────────────────────────
                CameraPreviewView(
                    modifier      = Modifier.fillMaxSize(),
                    onQrDetected  = { rawValue ->
                        if (!alreadyScanned) {
                            alreadyScanned = true
                            scanResult     = rawValue
                            scanSuccess    = true
                            showDialog     = true
                        }
                    }
                )

                // ── Marco de escaneo animado ──────────────────────────────────
                ScannerOverlay(scanSuccess = scanSuccess)

                // ── Texto de ayuda ────────────────────────────────────────────
                AnimatedVisibility(
                    visible  = !scanSuccess,
                    enter    = fadeIn(),
                    exit     = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint     = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text  = "Centra el código QR del cliente",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

            } else {
                // ── Sin permiso ───────────────────────────────────────────────
                NoCameraPermissionView {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

// ── Vista de cámara con ML Kit ────────────────────────────────────────────────
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewView(
    modifier     : Modifier = Modifier,
    onQrDetected : (String) -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor       = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val scanner = BarcodeScanning.getClient()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    if (barcode.valueType == Barcode.TYPE_TEXT ||
                                        barcode.rawValue != null
                                    ) {
                                        barcode.rawValue?.let { onQrDetected(it) }
                                        break
                                    }
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

// ── Overlay del marco de escaneo ──────────────────────────────────────────────
@Composable
fun ScannerOverlay(scanSuccess: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val lineY by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line_y"
    )

    val cornerColor = if (scanSuccess) Color(0xFF4CAF50) else Color.White

    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo oscuro semitransparente fuera del recuadro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // Marco central
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(Color.Transparent)
        ) {
            // Esquinas decorativas
            ScannerCorner(Alignment.TopStart,     cornerColor)
            ScannerCorner(Alignment.TopEnd,       cornerColor)
            ScannerCorner(Alignment.BottomStart,  cornerColor)
            ScannerCorner(Alignment.BottomEnd,    cornerColor)

            // Línea de escaneo animada (solo cuando no ha escaneado)
            if (!scanSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .padding(horizontal = 20.dp)
                        .offset(y = (lineY * 240).dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }

            // Ícono de éxito
            AnimatedVisibility(
                visible  = scanSuccess,
                enter    = scaleIn() + fadeIn(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier        = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

// ── Esquina del marco ─────────────────────────────────────────────────────────
@Composable
fun BoxScope.ScannerCorner(alignment: Alignment, color: Color) {
    val size         = 28.dp
    val strokeWidth  = 4.dp
    val borderRadius = 6.dp

    Box(
        modifier = Modifier
            .size(size)
            .align(alignment)
    ) {
        when (alignment) {
            Alignment.TopStart -> Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = strokeWidth,
                        color = color,
                        shape = RoundedCornerShape(topStart = borderRadius)
                    )
                    .clip(RoundedCornerShape(topStart = borderRadius))
            )
            Alignment.TopEnd -> Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = strokeWidth,
                        color = color,
                        shape = RoundedCornerShape(topEnd = borderRadius)
                    )
                    .clip(RoundedCornerShape(topEnd = borderRadius))
            )
            Alignment.BottomStart -> Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = strokeWidth,
                        color = color,
                        shape = RoundedCornerShape(bottomStart = borderRadius)
                    )
                    .clip(RoundedCornerShape(bottomStart = borderRadius))
            )
            Alignment.BottomEnd -> Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = strokeWidth,
                        color = color,
                        shape = RoundedCornerShape(bottomEnd = borderRadius)
                    )
                    .clip(RoundedCornerShape(bottomEnd = borderRadius))
            )
        }
    }
}

// ── Sin permiso de cámara ─────────────────────────────────────────────────────
@Composable
fun NoCameraPermissionView(onRequest: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.padding(32.dp)
    ) {
        Box(
            modifier        = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Permiso de cámara requerido",
            color      = Color.White,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "ParkMe necesita acceso a la cámara\npara escanear el código QR del cliente.",
            color     = Color.White.copy(alpha = 0.75f),
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onRequest,
            shape   = RoundedCornerShape(14.dp)
        ) {
            Text("Conceder permiso", fontWeight = FontWeight.SemiBold)
        }
    }
}