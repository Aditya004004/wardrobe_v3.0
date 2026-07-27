package com.example.wardeobe.model

import androidx.compose.runtime.Immutable

@Immutable
data class ClothingItem(
    val id: String, // 🌟 ADDED: Unique ID (e.g., Cloudinary public ID)
    val imageUrl: String,
    val category: String,
    val uploadDate: Long = System.currentTimeMillis() // 🌟 ADDED: Timestamp
) {
    companion object {
        fun fromMap(map: Map<String, Any>): ClothingItem {
            return ClothingItem(
                id = map["id"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                category = map["category"] as? String ?: "",
                uploadDate = (map["uploadDate"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

@Immutable
data class RecommendedOutfit(
    val topItem: ClothingItem?,
    val bottomItem: ClothingItem?,
    val recommendationText: String,
    val isFromWardrobe: Boolean
)