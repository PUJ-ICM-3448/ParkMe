package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.firebase.FirebaseChatRepository
import com.example.parkme.data.mock.MockAuth
import com.example.parkme.data.mock.MockParkingData
import com.example.parkme.data.model.Message
import com.example.parkme.notifications.ParkMeNotificationHelper
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, parkingId: Int) {

    val context     = LocalContext.current
    val currentUser = MockAuth.currentUser
    val parking     = MockParkingData.getParkingById(parkingId)

    var messageText by remember { mutableStateOf("") }
    var messages    by remember { mutableStateOf<List<Message>>(emptyList()) }

    val listState = rememberLazyListState()

    // ── Escuchar mensajes en tiempo real desde Firebase ───────────────────────
    LaunchedEffect(parkingId) {
        FirebaseChatRepository.observeMessages(parkingId).collect { newMessages ->
            messages = newMessages
            if (newMessages.isNotEmpty()) {
                listState.animateScrollToItem(newMessages.size - 1)
            }
        }
    }

    fun sendMessage() {
        val text = messageText.trim()
        if (text.isEmpty() || currentUser == null) return

        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = Message(
            sender    = currentUser.name,
            senderId  = currentUser.email,
            text      = text,
            timestamp = timestamp
        )
        FirebaseChatRepository.sendMessage(parkingId, msg)

        // Notificación local al enviar
        ParkMeNotificationHelper.showChatNotification(
            context     = context,
            senderName  = currentUser.name,
            messageText = text,
            parkingName = parking?.name ?: "Parqueadero"
        )
        messageText = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat — ${parking?.name ?: "Parqueadero"}", style = MaterialTheme.typography.titleMedium)
                        Text("Tiempo real · Firebase", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    }
                },
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
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {

            // ── Lista de mensajes ─────────────────────────────────────────────
            LazyColumn(
                state   = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == currentUser?.email
                    ChatBubble(message = msg, isMe = isMe)
                }
            }

            // ── Input ─────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = messageText,
                    onValueChange = { messageText = it },
                    modifier      = Modifier.weight(1f),
                    placeholder   = { Text("Escribe un mensaje...") },
                    shape         = RoundedCornerShape(24.dp),
                    maxLines      = 3
                )
                Spacer(Modifier.width(8.dp))
                FloatingActionButton(
                    onClick        = { sendMessage() },
                    modifier       = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Send, "Enviar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: Message, isMe: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!isMe) {
                Text(
                    text  = message.sender,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        color = if (isMe) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd   = if (isMe) 4.dp  else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 260.dp)
            ) {
                Column {
                    Text(
                        text  = message.text,
                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    if (message.timestamp.isNotEmpty()) {
                        Text(
                            text     = message.timestamp,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = if (isMe) Color.White.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}