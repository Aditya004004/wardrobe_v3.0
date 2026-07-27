package com.example.wardeobe.viewmodel

import com.example.wardeobe.BuildConfig
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.cloudinary.Cloudinary
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
import java.io.InputStream
import java.util.UUID
import com.example.wardeobe.repository.DeleteTokenRepository

/**
 * Handles image upload, AI processing (Freepik Gemini), and Cloudinary upload.
 */
@HiltViewModel
class UploadViewModel @Inject constructor(
    private val repository: com.example.wardeobe.data.WardrobeRepository
) : ViewModel() {

    // 🌟 Storage for delete tokens (used for secure client-side deletion of new items)
    // Delete tokens are now stored in DeleteTokenRepository

    // ⚠️ WARNING: Hardcoded API Key (Should be secured in production)
    private val FREEPIK_API_KEY = BuildConfig.FREEPIK_API_KEY
    private val FREEPIK_ENDPOINT = "https://api.magnific.com/v1/ai/gemini-2-5-flash-image-preview"

    // ⚠️ WARNING: Cloudinary Configuration
    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key", BuildConfig.CLOUDINARY_API_KEY,
            "api_secret", BuildConfig.CLOUDINARY_API_SECRET
        )
    )

    private val client = OkHttpClient()

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
                _uiState.value = UploadUiState(loading = true, userMessage = "Initializing upload...")

                val base64Image = uriToBase64(context, uri)
                if (base64Image == null) {
                    showError("Failed to convert image to Base64")
                    return@launch
                }

                // 🌟 USER-FACING: Generic AI processing message
                _uiState.value = _uiState.value.copy(userMessage = "Applying AI processing...")

                val aiUrl = generateWithFreepik(base64Image)
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

                if (uploadedUrl == null || publicId == null || deleteToken == null) {
                    showError("Cloudinary upload failed or missing tokens.")
                    return@launch
                }

                // 🌟 Store the delete token
                DeleteTokenRepository.deleteTokenMap[publicId] = deleteToken

                withContext(Dispatchers.Main) {
                    _uiState.value = UploadUiState(
                        loading = false,
                        userMessage = "Upload complete!",
                        imageUrl = uploadedUrl
                    )
                    onUploadComplete()
                }

            } catch (e: Exception) {
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
            Base64.encodeToString(compressed, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("UploadViewModel", "Base64 conversion failed", e)
            null
        }
    }

    // 🤖 Generate AI image using Freepik Gemini
    private suspend fun generateWithFreepik(base64Image: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val imageWithPrefix = "data:image/jpeg;base64,$base64Image"

                val jsonBody = JSONObject().apply {
                    put("prompt", "Remove background and place the clothing item on a mannequin.")
                    put("reference_images", JSONArray(listOf(imageWithPrefix)))
                }

                val requestBody =
                    jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(FREEPIK_ENDPOINT)
                    .addHeader("x-freepik-api-key", FREEPIK_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    Log.e("UploadViewModel", "❌ Freepik initial request failed: ${response.code}")
                    return@withContext null
                }

                val jsonResponse = JSONObject(responseBody ?: "")
                val taskId = jsonResponse.optJSONObject("data")?.optString("task_id")

                if (taskId.isNullOrEmpty()) {
                    Log.e("UploadViewModel", "❌ No task_id returned from Freepik.")
                    return@withContext null
                }

                pollFreepikTask(taskId)
            } catch (e: Exception) {
                Log.e("UploadViewModel", "generateWithFreepik Error", e)
                null
            }
        }

    // 🔄 Poll task until completed
    private suspend fun pollFreepikTask(taskId: String): String? =
        withContext(Dispatchers.IO) {
            val url = "https://api.freepik.com/v1/ai/gemini-2-5-flash-image-preview/$taskId"

            pollingLoop@ for (attempt in 0 until 20) {
                delay(3000)
                Log.d("UploadViewModel", "⏳ Waiting for AI image... attempt ${attempt + 1}")

                try {
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-freepik-api-key", FREEPIK_API_KEY)
                        .get()
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful || responseBody == null) {
                        Log.e("UploadViewModel", "❌ Polling failed with code: ${response.code}")
                        continue@pollingLoop
                    }

                    val json = JSONObject(responseBody)
                    val dataObject = json.optJSONObject("data")
                    val status = dataObject?.optString("status")

                    if (status == "COMPLETED") {
                        val generated = dataObject
                            ?.optJSONArray("generated")
                            ?.optString(0)

                        if (!generated.isNullOrEmpty()) {
                            Log.d("UploadViewModel", "✅ AI Image Ready: $generated")
                            return@withContext generated
                        } else {
                            Log.e(
                                "UploadViewModel",
                                "❌ COMPLETED status but no generated URL found."
                            )
                            return@withContext null
                        }
                    } else if (status == "FAILED" || status == "ERROR") {
                        Log.e("UploadViewModel", "❌ Freepik task FAILED. Response: $responseBody")
                        return@withContext null
                    }
                } catch (e: Exception) {
                    Log.e("UploadViewModel", "Polling request exception: ${e.message}", e)
                    continue@pollingLoop
                }
            }

            Log.e("UploadViewModel", "❌ Freepik task polling timed out")
            null
        }

    // ☁️ Upload to Cloudinary - Returns a Map for simplicity
    // 🌟 Updated to accept category (optionally used as a tag)
    private suspend fun uploadToCloudinary(
        imageUrl: String,
        category: String
    ): Map<*, *>? = withContext(Dispatchers.IO) {
        // 🌟 Get the current user ID for isolated storage
        val userId = Firebase.auth.currentUser?.uid ?: return@withContext null

        try {
            val userFolder = "wardrobe/$userId"

            val options = ObjectUtils.asMap(
                "folder", userFolder,
                "return_delete_token", true,
                // Optional: store category as a tag in Cloudinary
                "tags", category
            )

            cloudinary.uploader().upload(imageUrl, options)
        } catch (e: Exception) {
            Log.e("UploadViewModel", "Cloudinary upload failed", e)
            null
        }
    }

    /**
     * Uploads the local image URI to Cloudinary temporarily to get a public URL for the VTO AI.
     */
    suspend fun uploadTemporaryUri(context: Context, uri: Uri): Map<*, *>? =
        withContext(Dispatchers.IO) {
            val userId = Firebase.auth.currentUser?.uid ?: return@withContext null
            var inputStream: InputStream? = null

            try {
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e("UploadViewModel", "Input stream failed to open.")
                    return@withContext null
                }

                val publicId = "temp_vto_garment/${userId}_${UUID.randomUUID()}"

                val uploadResult = cloudinary.uploader().upload(
                    inputStream,
                    ObjectUtils.asMap(
                        "folder", "temp_vto_garment",
                        "public_id", publicId,
                        "resource_type", "auto",
                        "overwrite", true
                    )
                )

                uploadResult
            } catch (e: Exception) {
                Log.e("UploadViewModel", "Temporary Cloudinary upload failed: ${e.message}", e)
                null
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    Log.e("UploadViewModel", "Failed to close stream: ${e.message}")
                }
            }
        }

    // Updated showError
    private suspend fun showError(msg: String) {
        withContext(Dispatchers.Main) {
            _uiState.value = UploadUiState(
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
