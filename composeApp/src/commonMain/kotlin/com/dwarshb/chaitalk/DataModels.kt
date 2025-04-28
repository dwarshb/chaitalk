package com.dwarshb.chaitalk

data class Persona(
    val id: String,
    val name: String,
    val description: String,
    val personality: String,
    val personalityPrompt: String,
    val createdBy: String,
    val createdAt: Long
)