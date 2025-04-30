package com.dwarshb.chaitalk.recentChats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwarshb.chaitalk.ChatMessage
import com.dwarshb.chaitalk.Persona
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

class RecentChatsViewModel : ViewModel() {
    private val _recentChatList = MutableStateFlow(mapOf<String,ChatMessage>())
    val recentChats = _recentChatList.asStateFlow()
    var firebaseDatabase = FirebaseDatabase()

    fun fetchRecentChats(userId: String) {
        viewModelScope.launch {
            firebaseDatabase.readFirebaseDatabase(listOf("chats",userId),"",
                object : onCompletion<String>{
                    override fun onSuccess(t: String) {
                        if(t!="null") {
                            val rawJson = Json.decodeFromString<JsonObject>(t)
                            val parsedChats = rawJson.mapNotNull { (personaId, fieldsJsonElement) ->
                                val fields = fieldsJsonElement.jsonObject
                                fields["lastMessage"]?.let { lastMsgElement ->
                                    personaId to Json.decodeFromJsonElement<ChatMessage>(
                                        lastMsgElement
                                    )
                                }
                            }.toMap()
                            _recentChatList.value = parsedChats
                        }
                    }

                    override fun onError(e: Exception) {
                        e.printStackTrace()
                    }
                })
        }
    }

    fun getPersona(personaId: String,onCompletion: onCompletion<Persona>) {
        viewModelScope.launch {
            firebaseDatabase.readFirebaseDatabase(listOf("personas", personaId), "",
                object : onCompletion<String> {
                    override fun onSuccess(t: String) {
                        if (t!="null") {
                            val persona = Json { ignoreUnknownKeys = true }
                                .decodeFromString<Persona>(t)
                            onCompletion.onSuccess(persona)
                        }
                    }

                    override fun onError(e: Exception) {
                        onCompletion.onError(e)
                        e.printStackTrace()
                    }
                })
        }
    }
}