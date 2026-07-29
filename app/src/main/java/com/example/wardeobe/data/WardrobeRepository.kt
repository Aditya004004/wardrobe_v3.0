package com.example.wardeobe.data

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CancellationException
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
     * Uploads a local image URI temporarily via a Cloud Function for VTO.
     */
    suspend fun uploadTemporaryGarment(context: android.content.Context, uri: android.net.Uri): Map<*, *>? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val compressed = com.example.wardeobe.util.ImageCompressor.compressImage(context, uri) ?: return@withContext null
                val base64Image = android.util.Base64.encodeToString(compressed, android.util.Base64.NO_WRAP)

                val data = hashMapOf(
                    "imageBase64" to base64Image
                )

                val result = functions
                    .getHttpsCallable("uploadTemporaryGarment")
                    .call(data)
                    .await()
                
                result.data as? Map<*, *>
            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                Log.e("WardrobeRepository", "Temporary upload failed: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Deletes a temporary garment image via a Cloud Function.
     * Used by OutfitViewModel after VTO generation.
     */
    suspend fun deleteTemporaryGarment(publicId: String) {
        try {
            val data = hashMapOf("publicId" to publicId)
            functions
                .getHttpsCallable("deleteTemporaryGarment")
                .call(data)
                .await()
            Log.d("WardrobeRepository", "Deleted temporary garment: $publicId")
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            Log.e("WardrobeRepository", "Failed to delete temporary garment: $publicId", e)
        }
    }
}
