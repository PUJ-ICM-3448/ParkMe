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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

// Carga el Bitmap desde la ruta guardada en almacenamiento interno
private fun loadPhotoFromInternalStorage(path: String?): Bitmap? {
    if (path.isNullOrBlank()) return null
    val file = File(path)
    if (!file.exists()) return null
    return BitmapFactory.decodeFile(file.absolutePath)
}

@Composable
fun ProfileScreen(navController: NavController) {

    val user = MockAuth.currentUser ?: return
    val context = LocalContext.current

    var name  by remember { mutableStateOf(user.name) }
    var plate by remember { mutableStateOf(user.plate) }

    // Cargar foto previa desde almacenamiento interno si existe
    var profilePhoto by remember {
        mutableStateOf(loadPhotoFromInternalStorage(user.profilePhotoPath))
    }

    var photoMessage by remember {
        mutableStateOf(
            if (user.profilePhotoPath != null) "Foto de perfil cargada ✅"
            else "Sin foto de perfil seleccionada"
        )
    }

    // ── Cámara ───────────────────────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            profilePhoto = bitmap
            photoMessage = "📷 Foto tomada — presiona Guardar para conservarla"
        } else {
            photoMessage = "No se capturó ninguna foto"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else photoMessage = "Permiso de cámara denegado"
    }

    // ── Galería ──────────────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, uri)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                profilePhoto = bitmap
                photoMessage = "🖼️ Imagen seleccionada — presiona Guardar para conservarla"
            } catch (e: Exception) {
                photoMessage = "Error al cargar la imagen"
            }
        } else {
            photoMessage = "No se seleccionó ninguna imagen"
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Foto de perfil
        if (profilePhoto != null) {
            Image(
                bitmap = profilePhoto!!.asImageBitmap(),
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(110.dp).clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.profile_placeholder),
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(110.dp).clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = photoMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Botones Cámara y Galería lado a lado
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) cameraLauncher.launch(null)
                    else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cámara")
            }

            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Photo, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Galería")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = user.email, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Campos editables
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (user.role == "CLIENT") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it },
                        label = { Text("Placa del vehículo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Guardar: escribe la foto en filesDir y persiste la ruta en User ──
        Button(
            onClick = {
                // 1. Guardar foto en almacenamiento interno si hay una nueva
                val savedPath = if (profilePhoto != null) {
                    savePhotoToInternalStorage(
                        bitmap   = profilePhoto!!,
                        email    = user.email,
                        filesDir = context.filesDir
                    )
                } else {
                    user.profilePhotoPath  // conservar la ruta anterior
                }

                // 2. Actualizar usuario incluyendo la ruta de la foto
                val updatedUser = User(
                    name             = name,
                    email            = user.email,
                    password         = user.password,
                    plate            = plate,
                    role             = user.role,
                    profilePhotoPath = savedPath
                )

                MockAuth.updateUser(updatedUser)
                photoMessage = "Cambios guardados correctamente"
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar cambios")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                MockAuth.logout()
                navController.navigate(Routes.LOGIN)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}