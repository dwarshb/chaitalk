package com.dwarshb.chaitalk.personasView

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

class MyPersonaViewModel : ViewModel() {
    private val _personas = MutableStateFlow(listOf<Persona>())
    val personas = _personas.asStateFlow()
    var firebaseDatabase = FirebaseDatabase()

    fun fetchMyPersonas(userId: String) {
        viewModelScope.launch {
            firebaseDatabase.readFirebaseDatabase(listOf("personas"),
                "orderBy=\"createdBy\"&equalTo=\"${userId}\"",
                object : onCompletion<String> {
                    override fun onSuccess(t: String) {
                        if (t!="null") {
                            val rawJson = Json.decodeFromString<JsonObject>(t)
                            val parsedPersonas = rawJson.mapNotNull { (personaId, fieldsJsonElement) ->
                                val fields = fieldsJsonElement.jsonObject
                                personaId to Json.decodeFromJsonElement<Persona>(
                                        fields
                                )
                            }.toMap()

                            _personas.value = parsedPersonas.values.toMutableList()
                        }
                    }

                    override fun onError(e: Exception) {
                        e.printStackTrace()
                    }
                })
        }
    }
}