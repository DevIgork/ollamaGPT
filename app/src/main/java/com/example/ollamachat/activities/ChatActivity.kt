package com.example.ollamachat.activities

import ChatAdapter
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ollamachat.client.RetrofitClient
import com.example.ollamachat.databinding.ActivityChatBinding
import com.example.ollamachat.dto.OllamaRequest
import com.example.ollamachat.model.ChatMessage
import com.example.ollamachat.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var baseUrl: String
    private lateinit var modelName: String
    private lateinit var sendButton: FrameLayout
    private val senderId = "1"
    private val receiverId = "2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setListeners()
    }

    private fun init() {
        sendButton = binding.layoutSend
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(chatMessages, senderId)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = chatAdapter
        val intent = intent
        baseUrl = intent.getStringExtra("BASE_URL") ?: ""
        modelName = intent.getStringExtra("MODEL_NAME") ?: ""
        binding.titleName.text = modelName
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        sendButton.setOnClickListener {
            sendButton.isEnabled = false
            hideKeyboard(this)
            val message = binding.inputMessage.text.toString().trim()
            binding.inputMessage.text.clear()
            if (message.isNotEmpty()) {
                sendMessage(message, senderId)
                val apiService =
                    RetrofitClient.getClient(baseUrl).create(ApiService::class.java)
                lifecycleScope.launch {
                    var promptResult = sendRequest(message, apiService)
                    sendMessage(promptResult, receiverId)
                    sendButton.isEnabled = true
                }
            }
        }
    }

    private fun sendMessage(message: String, id: String) {
        val chatMessage = ChatMessage(message, id)
        chatMessages.add(chatMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
    }

    suspend fun sendRequest(prompt: String, apiService: ApiService): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.sendRequest(OllamaRequest(modelName, prompt, false)).execute()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.response
                } else {
                    "error"
                }
            } catch (e: Exception) {
                "error: ${e.message}"
            }
        }
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}
