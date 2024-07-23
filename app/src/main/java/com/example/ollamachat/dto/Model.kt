package com.example.ollamachat.dto

data class Model(
    val name: String,
    val model: String,
    val modified_at: String,
    val size: Long,
    val digest: String,
    val details: Details
)
