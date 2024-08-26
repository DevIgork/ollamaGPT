package com.example.ollamachat.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ollamachat.client.RetrofitClient
import com.example.ollamachat.databinding.ActivitySignInBinding
import com.example.ollamachat.service.ApiService
import kotlinx.coroutines.*
import retrofit2.HttpException

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var baseUrl: String
    private lateinit var modelName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.buttonEnter.setOnClickListener { handleSignIn() }
    }

    private fun handleSignIn() {
        hideErrors()
        initText()
        if (!checkUrl()) return

        CoroutineScope(Dispatchers.Main).launch {
            val serverUrl = getServerUrl()
            val apiService = RetrofitClient.getClient(serverUrl).create(ApiService::class.java)
            val passedUrlTest = async(Dispatchers.IO) { checkStatus(apiService) }.await()
            val passedModelTest = async(Dispatchers.IO) { checkModel(modelName, apiService) }.await()

            if (passedUrlTest && passedModelTest) {
                navigateToChatActivity(serverUrl)
            } else {
                handleErrors(passedUrlTest, passedModelTest)
            }
        }
    }

    private fun getServerUrl(): String {
        return when {
            baseUrl.startsWith("http") || baseUrl.startsWith("https") -> baseUrl
            else -> "http://$baseUrl/"
        }
    }

    private fun navigateToChatActivity(serverUrl: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("SERVER_URL", serverUrl)
            putExtra("MODEL_NAME", modelName)
        }
        startActivity(intent)
    }

    private fun handleErrors(passedUrlTest: Boolean, passedModelTest: Boolean) {
        if (!passedUrlTest) {
            vibrate()
            binding.urlError.visibility = View.VISIBLE
        }
        if (passedUrlTest && !passedModelTest) {
            vibrate()
            binding.modelError.visibility = View.VISIBLE
        }
    }

    private fun initText() {
        baseUrl = binding.InputUrl.text.toString().trim()
        modelName = binding.InputModel.text.toString().trim()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }


    private fun hideErrors() {
        binding.urlError.visibility = View.GONE
        binding.urlErrorFormat.visibility = View.GONE
        binding.modelError.visibility = View.GONE
    }

    private fun checkUrl(): Boolean {
        return if (baseUrl.isBlank()) {
            vibrate()
            binding.urlErrorFormat.visibility = View.VISIBLE
            false
        } else {
            true
        }
    }

    private fun checkStatus(apiService: ApiService): Boolean {
        return try {
            val response = apiService.checkStatus().execute()
            response.isSuccessful && response.body() == "Ollama is running"
        } catch (e: HttpException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun checkModel(modelName: String, apiService: ApiService): Boolean {
        return try {
            val response = apiService.getModels().execute()
            val models = response.body()?.models ?: return false
            val matchingModel = models.find { it.name.contains(modelName) }
            if (matchingModel != null) {
                this.modelName = matchingModel.name
                true
            } else {
                false
            }
        } catch (e: HttpException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
