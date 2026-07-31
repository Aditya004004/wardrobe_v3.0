package com.example.wardeobe.viewmodel

import com.example.wardeobe.BuildConfig
import kotlinx.coroutines.CancellationException
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * Handles image upload, AI processing (Gemini/Imagen), and Cloudinary upload.
 */
@HiltViewModel
class UploadViewModel @Inject constructor(
    private val aiGenerationRepository: com.example.wardeobe.data.AiGenerationRepository,
    private val wardrobeRepository: com.example.wardeobe.data.WardrobeRepository,
    private val functions: FirebaseFunctions
) : ViewModel() {

    // 🎯 UI State Flow for Compose observation
    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Initiates the full upload and AI generation process (For permanent wardrobe items).
     * 🌟 Updated to accept category.
     */
    fun uploadImageWithAI(
        context: Context,
        uri: Uri,
        category: String,
        onUploadComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 🌟 USER-FACING: Generic starting message
                _uiState.value = _uiState.value.copy(loading = true, userMessage = "Initializing upload...")

                val base64Image = uriToBase64(context, uri)
                if (base64Image == null) {
                    showError("Failed to convert image to Base64")
                    return@launch
                }

                // 🌟 USER-FACING: Generic AI processing message
                _uiState.value = _uiState.value.copy(userMessage = "Applying AI processing...")

                val aiUrl = generateWithAi(base64Image)
                if (aiUrl == null) {
                    showError("AI generation failed or timed out.")
                    return@launch
                }

                // 🌟 USER-FACING: Generic upload message
                _uiState.value = _uiState.value.copy(userMessage = "Finishing upload...")

                // 🌟 Upload to Cloudinary with category
                val uploadResult = uploadToCloudinary(aiUrl, category)
                val uploadedUrl = uploadResult?.get("secure_url") as? String
                val publicId = uploadResult?.get("public_id") as? String
                val deleteToken = uploadResult?.get("delete_token") as? String

                if (uploadedUrl == null || publicId == null) {
                    showError("Cloudinary upload failed.")
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        userMessage = "Upload complete!",
                        imageUrl = uploadedUrl
                    )
                    onUploadComplete()
                }

            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                Log.e("UploadViewModel", "❌ Upload failed: ${e.message}", e)
                showError("Unexpected error: ${e.message}")
            }
        }
    }

    // 🔁 Converts image URI to Base64 using compressed image bytes
    private suspend fun uriToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val compressed = com.example.wardeobe.util.ImageCompressor.compressImage(context, uri)
                ?: return@withContext null
            Base64.encodeToString(compressed, Base64.NO_WRAP)
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            Log.e("UploadViewModel", "Base64 conversion failed", e)
            null
        }
    }

    // 🤖 Generate AI image using AiGenerationRepository
    private suspend fun generateWithAi(base64Image: String): String? {
        val imageWithPrefix = "data:image/jpeg;base64,$base64Image"
        val prompt = "Remove background and place the clothing item on a mannequin."
        return aiGenerationRepository.generateImage(prompt, JSONArray(listOf(imageWithPrefix)))
    }

    // ☁️ Upload to Cloudinary - Returns a Map for simplicity
    // 🌟 Updated to accept category (optionally used as a tag)
    private suspend fun uploadToCloudinary(
        imageUrl: String,
        category: String
    ): Map<*, *>? = withContext(Dispatchers.IO) {
        try {
            val data = hashMapOf(
                "imageUrl" to imageUrl,
                "category" to category
            )
            val result = functions
                .getHttpsCallable("uploadWardrobeItemFromUrl")
                .call(data)
                .await()
            result.data as? Map<*, *>
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            Log.e("UploadViewModel", "Cloud function upload failed", e)
            null
        }
    }

    /**
     * Uploads the local image URI temporarily to get a public URL for the VTO AI.
     */
    suspend fun uploadTemporaryUri(context: Context, uri: Uri): Map<*, *>? {
        return wardrobeRepository.uploadTemporaryGarment(context, uri)
    }

    // Updated showError
    private suspend fun showError(msg: String) {
        withContext(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                userMessage = msg
            )
        }
    }
}

data class UploadUiState(
    val loading: Boolean = false,
    val userMessage: String = "",
    val imageUrl: String? = null
)
