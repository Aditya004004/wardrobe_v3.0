package com.example.wardeobe.data

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.example.wardeobe.model.ClothingItem
import kotlinx.coroutines.tasks.await
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.wardeobe.BuildConfig
import android.util.Log

class WardrobeRepository(
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore
) {
    /**
     * Calls a Cloud Function to fetch wardrobe items for the given user id.
     */
    suspend fun fetchItems(uid: String): List<ClothingItem> {
        val data = hashMapOf("uid" to uid)
        val result = functions
            .getHttpsCallable("fetchWardrobeItems")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        val items = (result.data as? List<Map<String, Any>>).orEmpty()
        return items.map { ClothingItem.fromMap(it) }
    }

    /**
     * Calls a Cloud Function to delete a clothing item.
     */
    suspend fun deleteItem(uid: String, publicId: String) {
        val data = hashMapOf("uid" to uid, "publicId" to publicId)
        functions
            .getHttpsCallable("deleteWardrobeItem")
            .call(data)
            .await()
    }

    /**
     * Uploads a new clothing item via a Cloud Function. The function returns the created ClothingItem.
     */
    suspend fun uploadItem(uid: String, base64Image: String, category: String): ClothingItem {
        val data = hashMapOf(
            "uid" to uid,
            "imageBase64" to base64Image,
            "category" to category
        )
        val result = functions
            .getHttpsCallable("uploadWardrobeItem")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        val map = result.data as Map<String, Any>
        return ClothingItem.fromMap(map)
    }
    /**
     * Deletes a temporary garment image from Cloudinary.
     * Used by OutfitViewModel after VTO generation.
     */
    suspend fun deleteTemporaryGarment(publicId: String) {
        val cloudinary = Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key", BuildConfig.CLOUDINARY_API_KEY,
                "api_secret", BuildConfig.CLOUDINARY_API_SECRET
            )
        )
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
            Log.d("WardrobeRepository", "Deleted temporary garment: $publicId")
        } catch (e: Exception) {
            Log.e("WardrobeRepository", "Failed to delete temporary garment: $publicId", e)
        }
    }
}
