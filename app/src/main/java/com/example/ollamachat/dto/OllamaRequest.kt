package com.example.ollamachat.dto

data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean
)
