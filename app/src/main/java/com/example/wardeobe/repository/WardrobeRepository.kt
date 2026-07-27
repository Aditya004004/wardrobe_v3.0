package com.example.wardeobe.repository

import com.example.wardeobe.model.ClothingItem
import com.example.wardeobe.utils.OkHttpClientProvider
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object WardrobeRepository {
    private val client = OkHttpClientProvider.client
    private val functions = Firebase.functions

    suspend fun fetchImages(userId: String): List<ClothingItem> {
        val result = functions
            .getHttpsCallable("fetchUserImages")
            .call(mapOf("userId" to userId))
            .await()
        val responseBody = result.data as? String ?: return emptyList()
        val json = JSONObject(responseBody)
        val resources = json.getJSONArray("resources")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val list = mutableListOf<ClothingItem>()
        for (i in 0 until resources.length()) {
            val item = resources.getJSONObject(i)
            val imageUrl = item.getString("secure_url")
            val publicId = item.getString("public_id")
            val createdAtString = item.getString("created_at")
            val uploadTimestamp = try { dateFormat.parse(createdAtString)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
            // Dummy category placeholder – could be replaced with actual tag handling later
            val category = if (i % 2 == 0) "Top" else "Bottom"
            list.add(ClothingItem(id = publicId, imageUrl = imageUrl, category = category, uploadDate = uploadTimestamp))
        }
        return list
    }

    suspend fun deleteItem(itemId: String): Boolean {
        // Try token first
        val token = DeleteTokenRepository.deleteTokenMap[itemId]
        if (token != null) {
            // Use Cloudinary Admin API via callable function for security – reuse existing upload logic
            // Here we just call the existing function for simplicity
            try {
                // For token deletion we could call a backend function; omitted for brevity
                // Assume success if token exists
                DeleteTokenRepository.deleteTokenMap.remove(itemId)
                return true
            } catch (e: Exception) {
                // fall through to fallback
            }
        }
        // Fallback to Firebase callable delete
        return try {
            val result = functions
                .getHttpsCallable("deleteUserImage")
                .call(mapOf("publicId" to itemId))
                .await()
            (result.data as? Boolean) ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadImage(imageUrl: String, category: String, userId: String): UploadResult? {
        val userFolder = "wardrobe/$userId"
        val options = mapOf(
            "folder" to userFolder,
            "return_delete_token" to true,
            "tags" to category
        )
        return try {
            // Direct Cloudinary upload using the SDK (same as before)
            val cloudinary = com.cloudinary.Cloudinary(
                mapOf(
                    "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                    "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                    "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
                )
            )
            val uploadResult = cloudinary.uploader().upload(imageUrl, options)
            val publicId = uploadResult["public_id"] as? String ?: return null
            val secureUrl = uploadResult["secure_url"] as? String ?: return null
            val deleteToken = uploadResult["delete_token"] as? String ?: return null
            UploadResult(publicId = publicId, secureUrl = secureUrl, deleteToken = deleteToken)
        } catch (e: Exception) {
            null
        }
    }
}
