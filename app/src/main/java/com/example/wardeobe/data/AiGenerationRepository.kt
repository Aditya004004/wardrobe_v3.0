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
                // Convert JSONArray to List<Map<String, String>> for Cloud Functions payload
                val referencesList = mutableListOf<Map<String, String>>()
                for (i in 0 until referenceImages.length()) {
                    val refObj = referenceImages.optJSONObject(i)
                    if (refObj != null) {
                        val map = mutableMapOf<String, String>()
                        val keys = refObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            map[key] = refObj.getString(key)
                        }
                        referencesList.add(map)
                    }
                }

                val data = hashMapOf(
                    "prompt" to prompt,
                    "reference_images" to referencesList
                )

                val result = functions
                    .getHttpsCallable("initiateAiGeneration")
                    .call(data)
                    .await()

                val responseData = result.data as? Map<*, *>
                val taskId = responseData?.get("task_id") as? String

                if (taskId.isNullOrEmpty()) {
                    Log.e(TAG, "❌ No task_id returned.")
                    return@withContext null
                }

                return@withContext pollTask(taskId)
            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                Log.e(TAG, "generateImage Error", e)
                null
            }
        }

    private suspend fun pollTask(taskId: String): String? =
        withContext(Dispatchers.IO) {
            pollingLoop@ for (attempt in 0 until 20) {
                delay(3000)
                Log.d(TAG, "⏳ Waiting for AI image... attempt ${attempt + 1}")

                try {
                    val data = hashMapOf("task_id" to taskId)
                    val result = functions
                        .getHttpsCallable("pollAiGeneration")
                        .call(data)
                        .await()

                    val responseData = result.data as? Map<*, *>
                    val status = responseData?.get("status") as? String

                    if (status == "COMPLETED") {
                        val generated = responseData?.get("generated_url") as? String
                        if (!generated.isNullOrEmpty()) {
                            Log.d(TAG, "✅ AI Image Ready: $generated")
                            return@withContext generated
                        } else {
                            Log.e(TAG, "❌ COMPLETED status but no generated URL found.")
                            return@withContext null
                        }
                    } else if (status == "FAILED" || status == "ERROR") {
                        Log.e(TAG, "❌ AI task FAILED. Response: $responseData")
                        return@withContext null
                    }
                } catch (e: CancellationException) { throw e } catch (e: Exception) {
                    Log.e(TAG, "Polling request exception: ${e.message}", e)
                    continue@pollingLoop
                }
            }

            Log.e(TAG, "❌ AI task polling timed out")
            null
        }
}
