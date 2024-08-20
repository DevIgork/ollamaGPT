package com.example.ollamachat.dto

data class OllamaRequestChat(
    val model: String,
    var messages : List<Messages>,
    val stream: Boolean
)
