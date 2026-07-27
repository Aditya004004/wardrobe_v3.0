package com.example.wardeobe.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiGenerationRepository(private val client: OkHttpClient) {
    companion object {
        private const val FREEPIK_ENDPOINT = "https://api.freepik.com/v1/ai/gemini-2-5-flash-image-preview"
    }

    /**
     * Sends the generation request and returns the task ID.
     */
    suspend fun generate(requestJson: JSONObject): String = withContext(Dispatchers.IO) {
        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(FREEPIK_ENDPOINT)
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Freepik generation failed: ${'$'}{response.code}")
            val respJson = JSONObject(response.body?.string() ?: "{}")
            // Assume API returns {"taskId": "..."}
            respJson.getString("taskId")
        }
    }

    /**
     * Polls the task endpoint until the result URL is ready.
     */
    suspend fun pollUntilComplete(taskId: String): String = withContext(Dispatchers.IO) {
        val pollUrl = "$FREEPIK_ENDPOINT/poll/$taskId"
        while (true) {
            val request = Request.Builder().url(pollUrl).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Polling failed: ${'$'}{response.code}")
                val respJson = JSONObject(response.body?.string() ?: "{}")
                if (respJson.getString("status") == "completed") {
                    return@withContext respJson.getString("resultUrl")
                }
                // Simple back‑off
                kotlinx.coroutines.delay(2000)
            }
        }
    }
}
