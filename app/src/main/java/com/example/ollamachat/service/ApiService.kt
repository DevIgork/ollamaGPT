package com.example.ollamachat.service

import com.example.ollamachat.dto.ModelResponse
import com.example.ollamachat.dto.OllamaRequest
import com.example.ollamachat.dto.OllamaRequestChat
import com.example.ollamachat.dto.OllamaResponse
import com.example.ollamachat.dto.OllamaResponseChat
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/")
    fun checkStatus(): Call<String>

    @GET("/api/tags")
    fun getModels(): Call<ModelResponse>

    @POST("/api/generate")
    fun sendRequest(@Body request: OllamaRequest): Call<OllamaResponse>

    @POST("/api/chat")
    fun sendRequest(@Body request: OllamaRequestChat): Call<OllamaResponseChat>
}