package com.example.ollamachat.dto

data class Details(
    val parent_model: String,
    val format: String,
    val family: String,
    val families: List<String>?,
    val parameter_size: String,
    val quantization_level: String
)
