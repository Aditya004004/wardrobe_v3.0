package com.example.wardeobe.viewmodel

import com.example.wardeobe.BuildConfig
import kotlinx.coroutines.CancellationException
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


import com.google.firebase.functions.FirebaseFunctions

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: com.example.wardeobe.data.ProfileRepository,
    private val functions: FirebaseFunctions,
    private val auth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    private val firestore = Firebase.firestore

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
                        val url = userModel.profilePictureUrl ?: ""
                        _uiState.value = _uiState.value.copy(
                            profilePictureUrl = url,
                            profilePicturePublicId = userModel.profilePicturePublicId
                        )
                        profileRepository.updateProfilePictureUrl(url)
                    }
                    Log.d("ProfileViewModel", "Profile fetched successfully.")
                }
            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching profile: ${e.message}")
            }
        }
    }

    /**
     * Uploads the image via Cloud Function and updates Firestore.
     */
    fun uploadProfilePicture(context: Context, uri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showError("Please log in to upload a picture.")
            return
        }

        _uiState.value = _uiState.value.copy(loading = true, message = "Uploading image...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val compressed = com.example.wardeobe.util.ImageCompressor.compressImage(context, uri)
                if (compressed == null) {
                    throw Exception("Could not compress image.")
                }
                val base64Image = android.util.Base64.encodeToString(compressed, android.util.Base64.NO_WRAP)

                val data = hashMapOf(
                    "imageBase64" to base64Image
                )

                val result = functions
                    .getHttpsCallable("uploadProfilePicture")
                    .call(data)
                    .await()

                val responseData = result.data as? Map<*, *>
                val url = responseData?.get("secure_url") as? String
                val publicId = responseData?.get("public_id") as? String

                if (url == null || publicId == null) {
                    throw Exception("Cloud function upload failed.")
                }

                // 2. Update Firestore UserModel with the new URL
                updateProfileUrlInDatabase(userId, url, publicId)

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        message = "Profile picture saved!",
                        profilePictureUrl = url,
                        profilePicturePublicId = publicId
                    )
                }
            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                Log.e("ProfileViewModel", "Upload failed: ${e.message}", e)
                showError("Upload failed: Check file permissions or try another image.")
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
                val data = hashMapOf("publicId" to publicId)
                functions
                    .getHttpsCallable("deleteProfilePicture")
                    .call(data)
                    .await()

                // Clear the URL in Firestore
                val userRef = firestore.collection("users").document(userId)
                val updates = hashMapOf(
                    "profilePictureUrl" to null,
                    "profilePicturePublicId" to null
                )
                userRef.update(updates as Map<String, Any>).await()

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        message = "Picture removed.",
                        profilePictureUrl = "",
                        profilePicturePublicId = null
                    )
                }
            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                Log.e("ProfileViewModel", "Deletion failed: ${e.message}", e)
                showError("Could not delete picture.")
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