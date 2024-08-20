package com.example.ollamachat.activities

import ChatAdapter
import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ollamachat.R
import com.example.ollamachat.client.RetrofitClient
import com.example.ollamachat.databinding.ActivityChatBinding
import com.example.ollamachat.dto.Messages
import com.example.ollamachat.dto.OllamaRequestChat
import com.example.ollamachat.model.ChatMessage
import com.example.ollamachat.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var ollamaRequestChat: OllamaRequestChat
    private lateinit var baseUrl: String
    private lateinit var modelName: String
    private lateinit var shortModelName: String
    private val senderId = "1"
    private val receiverId = "2"
    private var messagesList: MutableList<Messages> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        setListeners()
    }

    private fun initialize() {
        chatMessages = mutableListOf()
        chatAdapter = ChatAdapter(chatMessages, senderId)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
        baseUrl = intent.getStringExtra("SERVER_URL") ?: ""
        modelName = intent.getStringExtra("MODEL_NAME") ?: ""
        shortModelName = shortenModelName()
        binding.titleName.text = modelName
        ollamaRequestChat = OllamaRequestChat(modelName, listOf(), false)
    }

    private fun setListeners() {
        binding.apply {
            imageBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            imageInfo.setOnClickListener { showToast("Work in progress") } // TODO: Show model info
            attachIcon.setOnClickListener { showToast("Work in progress") } // TODO: Attach photo
            layoutSend.setOnClickListener { handleSendMessage() }
        }
    }

    private fun handleSendMessage() {
        val message = binding.inputMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            disableSendButton()
            hideKeyboard(this)
            updateTypingIndicator()
            binding.inputMessage.text.clear()
            sendMessage(message, senderId)
            lifecycleScope.launch {
                val apiService = RetrofitClient.getClient(baseUrl).create(ApiService::class.java)
                val response = sendRequest(apiService)
                sendMessage(response, receiverId)
                enableSendButton()
                hideTypingIndicator()
            }
        }
    }

    private fun disableSendButton() {
        binding.layoutSend.apply {
            isEnabled = false
            binding.sendIcon.setColorFilter(ContextCompat.getColor(this@ChatActivity, R.color.unavailable), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun enableSendButton() {
        binding.layoutSend.apply {
            isEnabled = true
            binding.sendIcon.setColorFilter(ContextCompat.getColor(this@ChatActivity, R.color.white), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun updateTypingIndicator() {
        binding.apply {
            ollamaTypingText.text = "$shortModelName is typing"
            modelTypingLayout.visibility = View.VISIBLE
        }
    }

    private fun hideTypingIndicator() {
        binding.modelTypingLayout.visibility = View.GONE
    }

    private fun shortenModelName(): String {
        return modelName.split(":").firstOrNull() ?: modelName
    }

    private fun sendMessage(message: String, id: String) {
        val messageType = if (id == senderId) "user" else "assistant"
        messagesList.add(Messages(messageType, message))
        chatMessages.add(ChatMessage(message, id))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
    }

    private suspend fun sendRequest(apiService: ApiService): String {
        return withContext(Dispatchers.IO) {
            try {
                ollamaRequestChat.messages = messagesList
                val response = apiService.sendRequest(ollamaRequestChat).execute()
                if (response.isSuccessful) {
                    response.body()?.message?.content ?: "No content"
                } else {
                    response.errorBody()?.source()?.toString() ?: "Error"
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
