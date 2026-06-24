package com.example.wardeobe.viewmodel

import com.example.wardeobe.BuildConfig
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardeobe.model.ClothingItem
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HomeViewModel(
    private val uploadViewModel: UploadViewModel // Dependency for secure token access
) : ViewModel() {

    private val client = OkHttpClient()

    private val _wardrobeItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val wardrobeItems: StateFlow<List<ClothingItem>> = _wardrobeItems.asStateFlow()

    // 🌟 NEW STATE: To track the currently selected filter category
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 🌟 NEW COMPUTED STATE: The filtered list displayed to the UI
    val filteredWardrobeItems: StateFlow<List<ClothingItem>> =
        combine(_wardrobeItems, _selectedCategory) { items, category ->
            if (category == "All") {
                items
            } else {
                items.filter { it.category == category }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )

    fun setFilterCategory(category: String) {
        _selectedCategory.value = category
    }

    // 🔹 Cloudinary credentials
    private val cloudName = "dfnjfuqbn"
    private val apiKey = "625179335524279"
    private val apiSecret = "kiNwgRaSZc7QwnMRjCGxIyQZ25I"
    private val folderName = "wardrobe"

    // Cloudinary instance required for Admin API access (fetch, delete fallback)
    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key", BuildConfig.CLOUDINARY_API_KEY,
            "api_secret", BuildConfig.CLOUDINARY_API_SECRET
        )
    )

    /**
     * Fetches all clothing items ONLY from the current user's isolated Cloudinary folder.
     */
    fun fetchImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val imageList = mutableListOf<ClothingItem>()

            // 🌟 FIX: Get the current user ID for data isolation
            val userId = Firebase.auth.currentUser?.uid
            if (userId == null) {
                Log.e("HomeViewModel", "User not authenticated. Cannot fetch wardrobe.")
                _wardrobeItems.value = emptyList()
                return@launch
            }

            try {
                val userFolderPrefix = "$folderName/$userId/"
                val url =
                    "https://api.cloudinary.com/v1_1/$cloudName/resources/image/upload?prefix=$userFolderPrefix&max_results=1000"

                val credentials = Credentials.basic(apiKey, apiSecret)

                val request = Request.Builder().url(url).header("Authorization", credentials).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val resources = json.getJSONArray("resources")

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                    for (i in 0 until resources.length()) {
                        val item = resources.getJSONObject(i)
                        val imageUrl = item.getString("secure_url")
                        val publicId = item.getString("public_id")

                        // ⚠️ Currently using dummy category; later you can read from Cloudinary tags/metadata
                        val category = if (i % 2 == 0) "Top" else "Bottom"

                        val createdAtString = item.getString("created_at")

                        val uploadTimestamp = try {
                            dateFormat.parse(createdAtString)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Date parse failed for $publicId: ${e.message}")
                            System.currentTimeMillis()
                        }

                        imageList.add(
                            ClothingItem(
                                id = publicId,
                                imageUrl = imageUrl,
                                category = category,
                                uploadDate = uploadTimestamp
                            )
                        )
                    }

                    Log.d("HomeViewModel", "✅ Loaded ${imageList.size} images for user $userId.")
                    _wardrobeItems.value = imageList

                } else {
                    Log.e("HomeViewModel", "❌ Failed to fetch: ${response.code}")
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "⚠️ Error fetching images: ${e.message}")
            }
        }
    }

    /**
     * 🗑️ Deletes a clothing item securely using a Delete Token or falls back to Admin API.
     */
    fun deleteClothingItem(itemId: String, onDeletionResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var success = false

            // Check for the secure delete token FIRST
            val deleteToken = uploadViewModel.deleteTokenMap[itemId]

            if (deleteToken != null) {
                // Method A: Secure Client-Side Delete with Token (Upload API)
                try {
                    val result = cloudinary.uploader().destroy(
                        itemId, ObjectUtils.asMap(
                            "token", deleteToken,
                            "resource_type", "image",
                            "invalidate", true
                        )
                    )

                    if (result["result"] == "ok") {
                        success = true
                        uploadViewModel.deleteTokenMap.remove(itemId)
                        Log.d("HomeViewModel", "✅ Item deleted securely with token: $itemId")
                    } else {
                        Log.e("HomeViewModel", "❌ Token deletion failed: ${result["error"]}")
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "⚠️ Error deleting with token: ${e.message}")
                }
            }

            // Fallback (Method B: Insecure Admin API deletion - should be removed in production)
            if (!success) {
                try {
                    val url = "https://api.cloudinary.com/v1_1/$cloudName/image/destroy"
                    val credentials = Credentials.basic(apiKey, apiSecret)
                    val encodedPublicId = URLEncoder.encode(itemId, "UTF-8")
                    val requestBody = "public_id=$encodedPublicId"
                        .toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())

                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", credentials)
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful) {
                        success = true
                        Log.d(
                            "HomeViewModel",
                            "✅ Item deleted via Admin API (Fallback): $itemId"
                        )
                    } else {
                        Log.e(
                            "HomeViewModel",
                            "❌ Deletion request failed: ${response.code}. Response: $responseBody"
                        )
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "⚠️ Error deleting via Admin API: ${e.message}")
                }
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    _wardrobeItems.update { currentList ->
                        currentList.filter { it.id != itemId }
                    }
                }
                onDeletionResult(success)
            }
        }
    }
}
