package com.dwarshb.chaitalk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(personaId: String,personaName: String) {
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var conversation by remember { mutableStateOf(listOf<String>()) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Chat History (LazyColumn)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(conversation.size) { index ->
                Text(
                    text = conversation[index],
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        // User Input Box
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f)
                    .padding(8.dp)
                    .background(Color.Gray.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
                    .padding(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (userInput.text.isNotEmpty()) {
                        sendMessage(userId =  "",
                            personaId = "",
                            userMessage = userInput.text,
                            conversation = conversation,
                            coroutineScope = coroutineScope) { response ->
                            conversation = conversation + "You: ${userInput.text}" + "$personaName: $response"
                            userInput = TextFieldValue("") // Clear input after sending
                        }
                    }
                },
                content = { Text("Send") }
            )
        }
    }
}

// Function to make the API call to Firebase Function
fun sendMessage(personaId: String,userId: String, userMessage: String, conversation: List<String>, coroutineScope: CoroutineScope, onMessageSent: (String) -> Unit) {
    coroutineScope.launch {
        val params = HashMap<String,Any>()
        params.put("personaId", personaId)
        params.put("sessionId", "$personaId-${userId}")  // Replace with dynamic session ID
        params.put("userMessage", userMessage)
    }
}