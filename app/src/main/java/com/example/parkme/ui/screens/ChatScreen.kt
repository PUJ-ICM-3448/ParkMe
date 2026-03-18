package com.example.parkme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkme.data.mock.*
import com.example.parkme.data.model.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, parkingId: Int) {

    val currentUser = MockAuth.currentUser
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }

    LaunchedEffect(Unit) {
        messages.clear()
        messages.addAll(MockChatData.getMessages(parkingId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
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

            LazyColumn(
                modifier = Modifier.weight(1f).padding(10.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                items(messages) { msg ->
                    val isMe = msg.sender == currentUser?.name

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                msg.text,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(10.dp)) {

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Mensaje...") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    val newMsg = Message(
                        id = System.currentTimeMillis().toInt(),
                        sender = currentUser?.name ?: "Yo",
                        text = message,
                        timestamp = ""
                    )
                    messages.add(newMsg)
                    MockChatData.sendMessage(parkingId, newMsg)
                    message = ""
                }) {
                    Text("Enviar")
                }
            }
        }
    }
}