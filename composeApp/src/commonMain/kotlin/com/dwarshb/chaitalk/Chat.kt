package com.dwarshb.chaitalk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dwarshb.firebase.Content
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.Gemini
import com.dwarshb.firebase.Part
import com.dwarshb.firebase.onCompletion
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(persona: Persona, onBackPressed: ()->Unit) {
    var messages = remember { mutableStateListOf<ChatMessage>() }
    var newMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isSendingText by remember { mutableStateOf("isSending...") }
    val userId = Firebase.getCurrentUser()?.uid?:""
    val gemini = Gemini()
    val firebaseDatabase = FirebaseDatabase()

    val scope = rememberCoroutineScope()
    val json = Json { ignoreUnknownKeys = true }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title =  {
                Row {
                    KamelImage(resource = { asyncPainterResource(persona.avatarUrl)},"avatar",
                        modifier = Modifier.size(32.dp))
                    Text(persona.name, modifier = Modifier.padding(start = 16.dp))
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    onBackPressed()
                }){
                    Icon(Icons.AutoMirrored.Default.ArrowBack,"Go Back")
                }
            }
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut()
                ) {
                    MessageItem(message)
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (isSending) {
            Text(isSendingText, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f).padding(12.dp),
                placeholder = {Text("Enter your Message")}
            )

            Button(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        scope.launch {
                            isSending = true
                            sendMessageToFirebase(
                                firebaseDatabase = firebaseDatabase,
                                avatarUrl = "",
                                role = "user",
                                content = newMessage,
                                userId = userId,
                                personaId = persona.id,
                                onCompletion = object : onCompletion<ChatMessage>{
                                    override fun onSuccess(t: ChatMessage) {
                                        messages.add(t)
                                        newMessage = ""
                                    }

                                    override fun onError(e: Exception) {
                                        e.printStackTrace()
                                    }
                                })
                            isSendingText = "AI Thinking..."
                            var aiMessage: ChatMessage? = null
                            gemini.conversationalAI(persona.personalityPrompt,
                                createContentFromConversation(messages),
                                object : onCompletion<String> {
                                    override fun onSuccess(t: String) {
                                        aiMessage = ChatMessage(
                                            id = Clock.System.now().toEpochMilliseconds(),
                                            role="model",
                                            content = t,
                                            avatarUrl = persona.avatarUrl
                                        )
                                    }

                                    override fun onError(e: Exception) {
                                        e.printStackTrace()
                                    }
                                })
                            sendMessageToFirebase(
                                firebaseDatabase = firebaseDatabase,
                                avatarUrl = persona.avatarUrl,
                                content = aiMessage?.content?:"",
                                userId = userId,
                                role = "model",
                                personaId = persona.id,
                                onCompletion = object : onCompletion<ChatMessage>{
                                    override fun onSuccess(t: ChatMessage) {
                                        messages.add(t)
                                        newMessage = ""
                                    }

                                    override fun onError(e: Exception) {
                                        e.printStackTrace()
                                    }
                                })
                            isSendingText = "isSending..."
                            isSending = false
                        }
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}

fun createContentFromConversation(messages: List<ChatMessage>): List<Content> {
    val contentList = mutableListOf<Content>()
    messages.forEach {
        val content = Content(role = it.role,
            parts = listOf(Part(text = it.content))
        )
        contentList.add(content)
    }
    return contentList
}
@Composable
fun MessageItem(message: ChatMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (message.role == "user") Color(0xFFD8E8FF) else Color(0xFFE8FFD8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            message.avatarUrl?.let { url ->
                KamelImage(resource = { asyncPainterResource(url) },"",
                    modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(8.dp))
            }

            Column {
                Text(
                    text = message.role.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

suspend fun sendMessageToFirebase(
    firebaseDatabase: FirebaseDatabase,
    role: String,
    avatarUrl: String,content: String, userId:String,
    personaId: String, onCompletion: onCompletion<ChatMessage>) {
    val params = HashMap<String,Any>()
    val message = ChatMessage(
        id = Clock.System.now().toEpochMilliseconds(),
        role = role,
        content = content,
        avatarUrl = avatarUrl)
    println("Message: ${message}")
    params.put(message.id.toString(),message)
    // Save the individual message
    if (userId.isNotEmpty()) {
        firebaseDatabase.patchFirebaseDatabase(listOf("chats", userId, personaId, "messages"),
            params, object : onCompletion<String> {
                override fun onSuccess(t: String) {
                    onCompletion.onSuccess(message)
                }

                override fun onError(e: Exception) {
                    e.printStackTrace()
                    onCompletion.onError(e)
                }
            })

        // Update last message
        params.clear()
        params.put("lastMessage", message)
        firebaseDatabase.patchFirebaseDatabase(listOf("chats", userId, personaId),
            params, object : onCompletion<String> {
                override fun onSuccess(t: String) {
                    println("Updated Last Message")
                }

                override fun onError(e: Exception) {
                    e.printStackTrace()
                }
            })
    } else{
        onCompletion.onSuccess(message)
    }
}