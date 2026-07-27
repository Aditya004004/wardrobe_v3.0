package com.example.wardeobe.viewmodel

import com.example.wardeobe.BuildConfig
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.wardeobe.model.UserModel
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.IOException
import java.util.UUID


@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    // Cloudinary is the primary storage for VTO profile images
    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key", BuildConfig.CLOUDINARY_API_KEY,
            "api_secret", BuildConfig.CLOUDINARY_API_SECRET
        )
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchUserProfile()
    }

    /**
     * Retrieves the current user's profile metadata (VTO URL) from Firestore.
     */
    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Fetch the existing user profile document
                val document = firestore.collection("users").document(userId).get().await()
                val userModel = document.toObject(UserModel::class.java)

                withContext(Dispatchers.Main) {
                    if (userModel != null) {
                        _uiState.value = _uiState.value.copy(
                            profilePictureUrl = userModel.profilePictureUrl ?: "",
                            profilePicturePublicId = userModel.profilePicturePublicId
                        )
                    }
                    Log.d("ProfileViewModel", "Profile fetched successfully.")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching profile: ${e.message}")
            }
        }
    }

    /**
     * Uploads the image to Cloudinary (bypassing Firebase Storage) and updates Firestore.
     */
    fun uploadProfilePicture(context: Context, uri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showError("Please log in to upload a picture.")
            return
        }

        _uiState.value = _uiState.value.copy(loading = true, message = "Uploading image...")

        viewModelScope.launch(Dispatchers.IO) {
            var inputStream: InputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    throw Exception("Could not open input stream from URI.")
                }

                // Pass the InputStream directly to Cloudinary
                val uploadResult = cloudinary.uploader().upload(
                    inputStream, // Use InputStream
                    ObjectUtils.asMap(
                        "folder", "profile_vto/$userId",
                        "public_id", userId,
                        "overwrite", true
                    )
                )

                val url = uploadResult["secure_url"] as? String
                val publicId = uploadResult["public_id"] as? String

                if (url == null || publicId == null) {
                    throw Exception("Cloudinary upload failed.")
                }

                // 2. Update Firestore UserModel with the new URL
                updateProfileUrlInDatabase(userId, url, publicId)

                withContext(Dispatchers.Main) {
                    _uiState.value = ProfileUiState(
                        loading = false,
                        message = "Profile picture saved!",
                        profilePictureUrl = url,
                        profilePicturePublicId = publicId
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Upload failed: ${e.message}", e)
                showError("Upload failed: Check file permissions or try another image.")
            } finally {
                // Safely close the InputStream regardless of success or failure
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    Log.e("ProfileViewModel", "Failed to close InputStream: ${e.message}")
                }
            }
        }
    }

    private suspend fun updateProfileUrlInDatabase(userId: String, url: String, publicId: String) {
        val userRef = firestore.collection("users").document(userId)
        val updates = hashMapOf(
            "profilePictureUrl" to url,
            "profilePicturePublicId" to publicId
        )
        userRef.update(updates as Map<String, Any>).await()
    }

    // --- Deletion Logic ---

    fun deleteProfilePicture() {
        val publicId = uiState.value.profilePicturePublicId
        val userId = auth.currentUser?.uid
        if (publicId == null || userId == null) return

        _uiState.value = _uiState.value.copy(loading = true, message = "Deleting picture...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Delete from Cloudinary
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())

                // Clear the URL in Firestore
                val userRef = firestore.collection("users").document(userId)
                val updates = hashMapOf(
                    "profilePictureUrl" to null,
                    "profilePicturePublicId" to null
                )
                userRef.update(updates as Map<String, Any>).await()

                withContext(Dispatchers.Main) {
                    _uiState.value = ProfileUiState(
                        loading = false,
                        message = "Picture removed.",
                        profilePictureUrl = ""
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Deletion failed: ${e.message}", e)
                showError("Could not delete picture. (Check Cloudinary Admin API access)")
            }
        }
    }

    private fun showError(msg: String) {
        _uiState.value = _uiState.value.copy(loading = false, message = msg)
    }
}

data class ProfileUiState(
    val loading: Boolean = false,
    val message: String = "",
    val profilePictureUrl: String = "",
    val profilePicturePublicId: String? = null
)