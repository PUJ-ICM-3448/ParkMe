package com.example.parkme.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.parkme.R
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.model.User
import com.example.parkme.navigation.Routes
import java.io.File
import java.io.FileOutputStream

private fun savePhotoToInternalStorage(bitmap: Bitmap, email: String, filesDir: File): String {
    val fileName = "profile_${email.replace("@", "_").replace(".", "_")}.jpg"
    val file = File(filesDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}

private fun loadPhotoFromInternalStorage(path: String?): Bitmap? {
    if (path.isNullOrBlank()) return null
    val file = File(path)
    if (!file.exists()) return null
    return BitmapFactory.decodeFile(file.absolutePath)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    val user    = MockAuth.currentUser ?: return
    val context = LocalContext.current

    var name  by remember { mutableStateOf(user.name) }
    var plate by remember { mutableStateOf(user.plate) }

    var profilePhoto by remember {
        mutableStateOf(loadPhotoFromInternalStorage(user.profilePhotoPath))
    }
    var photoMessage by remember {
        mutableStateOf(
            if (user.profilePhotoPath != null) "Foto de perfil cargada" else "Sin foto de perfil"
        )
    }
    var saveConfirmed by remember { mutableStateOf(false) }

    // ── Cámara ───────────────────────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            profilePhoto = bitmap
            photoMessage = "Foto tomada — guarda para conservarla"
        } else photoMessage = "No se capturó ninguna foto"
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else photoMessage = "Permiso de cámara denegado"
    }

    // ── Galería ──────────────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                profilePhoto = bitmap
                photoMessage = "Imagen seleccionada — guarda para conservarla"
            } catch (e: Exception) {
                photoMessage = "Error al cargar la imagen"
            }
        } else photoMessage = "No se seleccionó ninguna imagen"
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Cabecera con avatar ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(top = 40.dp, bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (profilePhoto != null) {
                        Image(
                            bitmap             = profilePhoto!!.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            modifier           = Modifier
                                .size(104.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Image(
                            painter            = painterResource(R.drawable.profile_placeholder),
                            contentDescription = "Foto de perfil",
                            modifier           = Modifier
                                .size(104.dp)
                                .clip(CircleShape)
                        )
                    }

                    // Badge de edición
                    Box(
                        modifier        = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onPrimary,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text       = name.ifBlank { user.name },
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text  = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                // Rol badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text     = if (user.role == "CLIENT") "Cliente" else "Arrendador",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text  = photoMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ── Cuerpo ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Botones de foto
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick  = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) cameraLauncher.launch(null)
                        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cámara")
                }

                OutlinedButton(
                    onClick  = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Galería")
                }
            }

            // Campos editables
            ElevatedCard(
                shape     = RoundedCornerShape(20.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        text       = "Información personal",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value         = name,
                        onValueChange = { name = it },
                        label         = { Text("Nombre completo") },
                        leadingIcon   = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value         = user.email,
                        onValueChange = {},
                        label         = { Text("Correo electrónico") },
                        leadingIcon   = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        enabled   = false,
                        singleLine = true
                    )

                    if (user.role == "CLIENT") {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value         = plate,
                            onValueChange = { plate = it },
                            label         = { Text("Placa del vehículo") },
                            leadingIcon   = {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null)
                            },
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Confirmación guardado
            if (saveConfirmed) {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "✅ Cambios guardados correctamente",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Guardar
            Button(
                onClick  = {
                    val savedPath = if (profilePhoto != null) {
                        savePhotoToInternalStorage(
                            bitmap   = profilePhoto!!,
                            email    = user.email,
                            filesDir = context.filesDir
                        )
                    } else user.profilePhotoPath

                    val updatedUser = User(
                        name             = name,
                        email            = user.email,
                        password         = user.password,
                        plate            = plate,
                        role             = user.role,
                        profilePhotoPath = savedPath
                    )
                    MockAuth.updateUser(updatedUser)
                    photoMessage  = "Foto de perfil cargada"
                    saveConfirmed = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
            }

            // Cerrar sesión
            OutlinedButton(
                onClick  = {
                    MockAuth.logout()
                    navController.navigate(Routes.LOGIN)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}