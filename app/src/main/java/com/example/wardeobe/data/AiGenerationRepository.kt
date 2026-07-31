package com.example.wardeobe.data

import android.util.Log
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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

    suspend fun generateImage(prompt: String, referenceImages: JSONArray = JSONArray()): String? =
        withContext(Dispatchers.IO) {
            try {
                // 1. Call Google AI Studio (Gemini 3.1 Flash Image) directly via REST API
                val client = OkHttpClient()
                val apiKey = com.example.wardeobe.BuildConfig.GEMINI_API_KEY
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image:generateContent?key=$apiKey"

                val partsArray = JSONArray().put(JSONObject().put("text", prompt))
                
                // If you want to support reference images later, you'd add them to partsArray here
                // Note: The previous implementation completely ignored referenceImages.

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

                if (!response.isSuccessful || responseString.isNullOrEmpty()) {
                    Log.e(TAG, "❌ Gemini API Error: ${response.code} ${response.message} - $responseString")
                    return@withContext null
                }

                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val responseParts = content?.optJSONArray("parts")
                
                var base64Image: String? = null
                
                if (responseParts != null) {
                    for (i in 0 until responseParts.length()) {
                        val part = responseParts.optJSONObject(i)
                        val inlineData = part?.optJSONObject("inlineData")
                        if (inlineData != null) {
                            base64Image = inlineData.optString("data")
                            break
                        }
                    }
                }

                if (base64Image.isNullOrEmpty()) {
                    Log.e(TAG, "❌ No image bytes returned from Gemini. Response: $responseString")
                    return@withContext null
                }

                Log.d(TAG, "✅ Gemini Image Generated. Uploading to Cloudinary...")

                // 2. Upload the Base64 image using the existing Cloud Function
                val data = hashMapOf("imageBase64" to base64Image)
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
                null
            }
        }
}
