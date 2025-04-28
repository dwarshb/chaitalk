package com.dwarshb.chaitalk

import kotlinx.serialization.Serializable

@Serializable
data class Persona(
    val id: String,
    val name: String,
    val description: String,
    val personality: String,
    val personalityPrompt: String,
    val createdBy: String,
    val createdAt: Long
)