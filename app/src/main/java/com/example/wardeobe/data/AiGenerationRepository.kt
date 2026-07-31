package com.example.wardeobe.data

import android.util.Log
import com.example.wardeobe.BuildConfig
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

@Singleton
class AiGenerationRepository @Inject constructor(
    private val functions: FirebaseFunctions
) {
    companion object {
        private const val TAG = "AiGenerationRepository"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    private suspend fun downloadImageAsBase64(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                if (bytes != null) {
                    return@withContext android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download reference image: $url", e)
        }
        return@withContext null
    }

    suspend fun generateImage(prompt: String, referenceImages: JSONArray = JSONArray()): String? =
        withContext(Dispatchers.IO) {
            try {
                // 1. Call Google AI Studio (Gemini 3.1 Flash Image) directly via REST API
                val apiKey = BuildConfig.GEMINI_API_KEY
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image:generateContent?key=$apiKey"

                val partsArray = JSONArray().put(JSONObject().put("text", prompt))
                
                // Add reference images to partsArray
                for (i in 0 until referenceImages.length()) {
                    val imageStr = referenceImages.optString(i)
                    if (imageStr.isNullOrEmpty()) continue

                    val base64Data = if (imageStr.startsWith("http", ignoreCase = true)) {
                        downloadImageAsBase64(imageStr)
                    } else if (imageStr.startsWith("data:image")) {
                        imageStr.substringAfter("base64,")
                    } else {
                        imageStr // Fallback if already base64
                    }

                    if (!base64Data.isNullOrEmpty()) {
                        partsArray.put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Data)
                            })
                        })
                    }
                }

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", partsArray)
                    ))
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseString = response.body?.string()
                
                if (response.code == 429) {
                    Log.e(TAG, "❌ Quota exceeded. Please check your Google AI Studio billing and limits. Response: $responseString")
                    throw Exception("Quota exceeded. Please check your Google AI Studio billing and limits.")
                }

                if (!response.isSuccessful || responseString.isNullOrEmpty()) {
                    Log.e(TAG, "❌ Gemini API Error: ${response.code} ${response.message} - $responseString")
                    return@withContext null
                }

                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val responseParts = content?.optJSONArray("parts")
                
                var generatedBase64: String? = null
                
                if (responseParts != null) {
                    for (i in 0 until responseParts.length()) {
                        val part = responseParts.optJSONObject(i)
                        val inlineData = part?.optJSONObject("inlineData")
                        if (inlineData != null) {
                            generatedBase64 = inlineData.optString("data")
                            break
                        }
                    }
                }

                if (generatedBase64.isNullOrEmpty()) {
                    Log.e(TAG, "❌ No image bytes returned from Gemini. Response: $responseString")
                    return@withContext null
                }

                Log.d(TAG, "✅ Gemini Image Generated. Uploading to Cloudinary...")

                // 2. Upload the Base64 image using the existing Cloud Function
                // Intentionally using temporary garment upload for VTO and processing results.
                // Permanent saving is handled separately if the user chooses to save the result.
                val data = hashMapOf("imageBase64" to generatedBase64)
                val result = functions
                    .getHttpsCallable("uploadTemporaryGarment")
                    .call(data)
                    .await()

                val responseData = result.data as? Map<*, *>
                val generatedUrl = responseData?.get("secure_url") as? String

                if (generatedUrl.isNullOrEmpty()) {
                    Log.e(TAG, "❌ Cloudinary upload failed. No secure_url returned.")
                    return@withContext null
                }
                
                Log.d(TAG, "✅ AI Image Ready & Uploaded: $generatedUrl")
                return@withContext generatedUrl

            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                Log.e(TAG, "generateImage Error", e)
                if (e.message?.contains("Quota exceeded") == true) throw e
                null
            }
        }
}
