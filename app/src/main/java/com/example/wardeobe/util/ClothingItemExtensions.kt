package com.example.wardeobe.util

import com.example.wardeobe.model.ClothingItem

fun ClothingItem.thumbnailUrl(): String {
    // Cloudinary transformation for a 200x200 thumbnail using fill mode
    return if (imageUrl.contains("/upload/")) {
        imageUrl.replace("/upload/", "/upload/c_fill,w_200,h_200/")
    } else {
        imageUrl
    }
}
