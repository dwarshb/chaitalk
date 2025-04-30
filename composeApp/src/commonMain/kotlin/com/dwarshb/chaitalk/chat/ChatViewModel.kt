package com.dwarshb.chaitalk.chat

import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwarshb.chaitalk.ChatMessage
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow(mutableMapOf<String, ChatMessage>())
    val messages = _messages.asStateFlow()
    var firebaseDatabase = FirebaseDatabase()

    fun fetchMessages(userId: String,personaId: String) {
        viewModelScope.launch {
            firebaseDatabase.readFirebaseDatabase(listOf("chats", userId, personaId, "messages"),
                "", object : onCompletion<String> {
                    override fun onSuccess(t: String) {
                        if (t!="null") {
                            println(t)
                            val rawJson = Json.decodeFromString<JsonObject>(t)
                            val parsedChats = rawJson.mapNotNull { (messageId, fieldsJsonElement) ->
                                val fields = fieldsJsonElement.jsonObject
                                messageId to Json.decodeFromJsonElement<ChatMessage>(
                                        fields
                                )
                            }.toMutableStateMap()
                            _messages.value = parsedChats
                        }
                    }

                    override fun onError(e: Exception) {
                        e.printStackTrace()
                    }
                })
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
}