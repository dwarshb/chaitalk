package com.dwarshb.chaitalk

import kotlinx.serialization.Serializable

@Serializable
data class Persona(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val description: String,
    val personality: String,
    val personalityPrompt: String,
    val createdBy: String,
    val createdAt: Long
)

@Serializable
data class ChatMessage(
    val id: Long,
    val role: String,
    val content: String,
    var avatarUrl: String = "",
)

@Serializable
data class AppUser(
    var email: String = "",
    var profile: String = "",
    var personaList: List<Persona> = emptyList(),
    var uid: String = ""
)