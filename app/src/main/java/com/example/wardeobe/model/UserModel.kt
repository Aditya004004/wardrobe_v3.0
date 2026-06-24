package com.example.wardeobe.model

/**
 * Represents the extended user profile data stored in Firebase Firestore.
 * The document ID for this collection will be the Firebase User ID (UID).
 * * NOTE: All properties must be mutable (var) or nullable for Firestore's automatic
 * data mapping (toObject()).
 */
data class UserModel(
    // The Firebase User ID (UID) - acts as the primary key/document ID
    // This MUST match the field used when saving the user document in AuthViewModel.
    var userId: String = "",

    // Virtual Try-On related fields (Stored securely in Firebase Storage)
    // Firestore maps 'null' directly, which is useful for checking if a picture exists.
    var profilePictureUrl: String? = null,
    var profilePicturePublicId: String? = null, // Firebase Storage path for deletion

    // Demographic/Style fields (synced from ProfileSetupScreen)
    var gender: String = "",
    var bodyType: String = "",
    var skinTone: String = "",
    var ageGroup: String = "",
    var heightGroup: String = ""
)