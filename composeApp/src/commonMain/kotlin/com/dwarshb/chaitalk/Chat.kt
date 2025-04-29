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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dwarshb.firebase.Content
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.Gemini
import com.dwarshb.firebase.Part
import com.dwarshb.firebase.onCompletion
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long


val firebaseDatabase = FirebaseDatabase()
val gemini = Gemini()

@Composable
fun ChatScreen(persona: Persona) {
    var messages = remember { mutableStateListOf<ChatMessage>() }
    var newMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isSendingText by remember { mutableStateOf("isSending...") }
    val userId = Firebase.getCurrentUser()?.uid?:""
    val scope = rememberCoroutineScope()
    val json = Json { ignoreUnknownKeys = true }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("chAItalk", style = MaterialTheme.typography.headlineSmall)

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
            BasicTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f).padding(12.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        scope.launch {
                            isSending = true
                            sendMessageToFirebase(
                                avatarUrl = "",
                                content = newMessage,
                                userIdPersonaId = userId+"_"+persona.id,
                                object : onCompletion<ChatMessage>{
                                    override fun onSuccess(t: ChatMessage) {
                                        messages.add(t)
                                        newMessage = ""
                                    }

                                    override fun onError(e: Exception) {
                                        e.printStackTrace()
                                    }
                                })
                            isSendingText = "AI Thinking..."
                            gemini.conversationalAI(persona.personalityPrompt,
                                createContentFromConversation(messages),
                                object : onCompletion<String> {
                                    override fun onSuccess(t: String) {
                                        val aiMessage = ChatMessage(
                                            id = Clock.System.now().toEpochMilliseconds(),
                                            role="model",
                                            content = t,
                                            avatarUrl = persona.avatarUrl
                                        )
                                        messages.add(aiMessage)
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

suspend fun sendMessageToFirebase(avatarUrl: String,content: String, userIdPersonaId: String,
                                  onCompletion: onCompletion<ChatMessage>) {
    val params = HashMap<String,Any>()
    val message = ChatMessage(
        id = Clock.System.now().toEpochMilliseconds(),
        role = "user",
        content = content,
        avatarUrl = avatarUrl)
    println("Message: ${message}")
    params.put(message.id.toString(),message)
    firebaseDatabase.patchFirebaseDatabase(listOf("chats",userIdPersonaId,"messages"),
        params, object : onCompletion<String>{
            override fun onSuccess(t: String) {
                onCompletion.onSuccess(message)
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                onCompletion.onError(e)
            }
        })

//    val message = buildJsonObject {
//        put("role", "user")
//        put("content", content)
//        put("avatar", "https://avatar.iran.liara.run/public/${(0..1000).random()}")
//    }
}