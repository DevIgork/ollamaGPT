package com.example.ollamachat.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ollamachat.client.RetrofitClient
import com.example.ollamachat.databinding.ActivitySignInBinding
import com.example.ollamachat.service.ApiService
import com.makeramen.roundedimageview.BuildConfig
import kotlinx.coroutines.*
import retrofit2.HttpException

class SignInActivity : AppCompatActivity() {
    private lateinit var enterButton: Button
    private lateinit var binding: ActivitySignInBinding
    private lateinit var baseUrl: String
    private lateinit var modelName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("kotlinx.coroutines.debug", if(BuildConfig.DEBUG) "on" else "off")
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setListeners()
    }

    private fun setListeners() {
        enterButton.setOnClickListener {
            hideErrors()
            initText()
            if (checkUrl()) {
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.Main).launch {
                val apiService = RetrofitClient.getClient(baseUrl).create(ApiService::class.java)
                val passedUrlTestDeferred = async(Dispatchers.IO) { checkStatus(apiService) }
                val passedModelTestDeferred = async(Dispatchers.IO) { checkModel(modelName, apiService) }
                val passedUrlTest = passedUrlTestDeferred.await()
                val passedModelTest = passedModelTestDeferred.await()
                if (passedUrlTest && passedModelTest) {
                    val intent = Intent(this@SignInActivity, ChatActivity::class.java).apply {
                        putExtra("BASE_URL", baseUrl)
                        putExtra("MODEL_NAME", modelName)
                    }
                    startActivity(intent)
                } else {
                    if (!passedUrlTest) {
                        vibrate()
                        binding.urlError.visibility = View.VISIBLE
                    }
                    if (!passedModelTest) {
                        vibrate()
                        binding.modelError.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun init() {
        enterButton = binding.buttonEnter
    }

    private fun initText() {
        baseUrl = binding.InputUrl.text.toString().trim()
        modelName = binding.InputModel.text.toString().trim()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
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

    private fun checkUrl() : Boolean {
        if (baseUrl.isBlank() || (!baseUrl.startsWith("http") && !baseUrl.startsWith("https"))) {
            vibrate()
            binding.urlErrorFormat.visibility = View.VISIBLE
            return true
        }
        return false
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
