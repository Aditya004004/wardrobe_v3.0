package com.example.wardeobe.viewmodel

import com.example.wardeobe.BuildConfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.wardeobe.model.ClothingItem
import com.example.wardeobe.model.RecommendedOutfit
import com.example.wardeobe.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log
import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import android.content.Context
import java.util.UUID

@HiltViewModel
class OutfitViewModel @Inject constructor(
    private val profileViewModel: ProfileViewModel,
    private val uploadViewModel: UploadViewModel,
    private val repository: com.example.wardeobe.data.WardrobeRepository
) : ViewModel() {

    private val FREEPIK_API_KEY = BuildConfig.FREEPIK_API_KEY
    private val FREEPIK_ENDPOINT = "https://api.magnific.com/v1/ai/gemini-2-5-flash-image-preview"

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    var selectedOccasion: String = ""
    var selectedStyle: String = ""
    var recommendationType: String = "personal"

    private val _shoppingImageUrl = MutableStateFlow<String?>(null)
    val shoppingImageUrl: StateFlow<String?> = _shoppingImageUrl.asStateFlow()
    private val _isGeneratingShoppingOutfit = MutableStateFlow(false)
    val isGeneratingShoppingOutfit: StateFlow<Boolean> = _isGeneratingShoppingOutfit.asStateFlow()

    private val _vtoImageUrl = MutableStateFlow<String?>(null)
    val vtoImageUrl: StateFlow<String?> = _vtoImageUrl.asStateFlow()
    private val _isGeneratingVTO = MutableStateFlow(false)
    val isGeneratingVTO: StateFlow<Boolean> = _isGeneratingVTO.asStateFlow()

    private var cachedProfileUrl: String? = null

    private val client = OkHttpClient()

    init {
        viewModelScope.launch {
            profileViewModel.uiState.collect { state ->
                cachedProfileUrl = state.profilePictureUrl.ifEmpty { null }
            }
        }
    }

    fun updateGender(gender: String) { _userProfile.update { it.copy(gender = gender) } }
    fun updateBodyType(bodyType: String) { _userProfile.update { it.copy(bodyType = bodyType) } }
    fun updateSkinTone(skinTone: String) { _userProfile.update { it.copy(skinTone = skinTone) } }
    fun updateAgeGroup(ageGroup: String) { _userProfile.update { it.copy(ageGroup = ageGroup) } }
    fun updateHeightGroup(heightGroup: String) { _userProfile.update { it.copy(heightGroup = heightGroup) } }

    fun resetShoppingOutfit() {
        _shoppingImageUrl.value = null
        _isGeneratingShoppingOutfit.value = false
        _vtoImageUrl.value = null
        _isGeneratingVTO.value = false
    }

    fun startVtoLocalGeneration(context: Context, garmentUri: Uri) {
        if (_isGeneratingVTO.value) return

        resetShoppingOutfit()
        _isGeneratingVTO.value = true

        profileViewModel.fetchUserProfile()

        viewModelScope.launch {
            val profileUrl = cachedProfileUrl

            val uploadResult = uploadViewModel.uploadTemporaryUri(context, garmentUri)
            val publicGarmentUrl = uploadResult?.get("secure_url") as? String
            val tempPublicId = uploadResult?.get("public_id") as? String

            if (publicGarmentUrl.isNullOrEmpty()) {
                Log.e("OutfitViewModel", "Temporary garment upload failed.")
                _isGeneratingVTO.value = false
                return@launch
            }

            val vtoUrl = if (profileUrl.isNullOrEmpty()) {
                generateVtoImageFallback(publicGarmentUrl)
            } else {
                generateVtoImage(publicGarmentUrl, profileUrl)
            }

            _vtoImageUrl.value = vtoUrl
            _isGeneratingVTO.value = false

            if (!tempPublicId.isNullOrEmpty()) {
                cleanUpTemporaryGarment(tempPublicId)
            }
        }
    }

    fun startShoppingOutfitGeneration() {
        if (_isGeneratingShoppingOutfit.value || _isGeneratingVTO.value) return

        resetShoppingOutfit()
        _isGeneratingShoppingOutfit.value = true

        profileViewModel.fetchUserProfile()

        viewModelScope.launch {
            val shopUrl = generateNewOutfitWithFreepik()
            _shoppingImageUrl.value = shopUrl
            _isGeneratingShoppingOutfit.value = false

            val profileUrl = cachedProfileUrl
            if (!shopUrl.isNullOrEmpty() && !profileUrl.isNullOrEmpty()) {
                startVtoGeneration(shopUrl, profileUrl)
            }
        }
    }

    private fun startVtoGeneration(shopImageUrl: String, profileUrl: String) {
        _isGeneratingVTO.value = true
        _vtoImageUrl.value = null

        viewModelScope.launch {
            val vtoUrl = generateVtoImage(shopImageUrl, profileUrl)
            _vtoImageUrl.value = vtoUrl
            _isGeneratingVTO.value = false
        }
    }

    private suspend fun initiateFreepikGeneration(prompt: String, referenceImages: JSONArray): String? =
        withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("prompt", prompt)
                    put("reference_images", referenceImages)
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(FREEPIK_ENDPOINT)
                    .addHeader("x-freepik-api-key", FREEPIK_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    Log.e("OutfitViewModel", "❌ Freepik API initial request failed: ${response.code}. Response: $responseBody")
                    return@withContext null
                }

                val jsonResponse = JSONObject(responseBody ?: "")
                val taskId = jsonResponse.optJSONObject("data")?.optString("task_id")

                return@withContext pollFreepikTask(taskId ?: "")

            } catch (e: Exception) {
                Log.e("OutfitViewModel", "Freepik Generation Initiation Error: ${e.message}", e)
                null
            }
        }

    // -------------------------
    // 🔄 UPDATED PROMPT BLOCKS
    // -------------------------

    private suspend fun generateVtoImage(outfitUrl: String, profileUrl: String): String? =
        withContext(Dispatchers.IO) {

            val prompt = buildString {
                append("Perform a flawless virtual try-on. Seamlessly merge the garment from the first reference image onto the body of the person in the second reference image. ")
                append("The cloth must follow the correct perspective, lighting, and natural shadow of the model's body, ensuring a realistic fit. Hyper-realistic, studio quality.")
            }

            val referenceImages = JSONArray(listOf(outfitUrl, profileUrl))
            return@withContext initiateFreepikGeneration(prompt, referenceImages)
        }

    private suspend fun generateVtoImageFallback(garmentUrl: String): String? =
        withContext(Dispatchers.IO) {
            val profile = userProfile.value

            val prompt = buildString {
                append("Render the garment from the provided image onto a photorealistic, full-body mannequin. ")
                append("The mannequin must represent a ${profile.gender}, ${profile.bodyType} body type. ")
                append("The background must be clean and neutral. High resolution.")
            }

            val referenceImages = JSONArray(listOf(garmentUrl))
            return@withContext initiateFreepikGeneration(prompt, referenceImages)
        }

    private suspend fun generateNewOutfitWithFreepik(): String? =
        withContext(Dispatchers.IO) {
            val profile = userProfile.value

            val detailedPrompt = buildString {
                append("Generate a high-resolution, hyper-realistic image of a single, complete outfit in a perfectly lit, professional studio flat lay style. ")
                append("The outfit must be ${selectedStyle} and suitable for a ${selectedOccasion} event. ")
                append("Based on a ${profile.gender}, ${profile.bodyType} body type, and ${profile.skinTone} skin tone. ")
                append("Focus on clear separation of the pieces on a simple, neutral background. ")
                append("Render the fabrics with realistic texture and depth. Photorealistic, 8K quality.")
            }

            return@withContext initiateFreepikGeneration(detailedPrompt, JSONArray())
        }

    private suspend fun pollFreepikTask(taskId: String): String? =
        withContext(Dispatchers.IO) {
            val url = "https://api.magnific.com/v1/ai/gemini-2-5-flash-image-preview/$taskId"

            pollingLoop@ for (attempt in 0 until 20) {
                delay(3000)

                try {
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-freepik-api-key", FREEPIK_API_KEY)
                        .get()
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful || responseBody == null) {
                        Log.e("OutfitViewModel", "❌ Polling failed with code: ${response.code}. Response: $responseBody")
                        continue@pollingLoop
                    }

                    val json = JSONObject(responseBody)
                    val dataObject = json.optJSONObject("data")
                    val status = dataObject?.optString("status")

                    if (status == "COMPLETED") {
                        val generated = dataObject?.optJSONArray("generated")?.optString(0)

                        if (!generated.isNullOrEmpty()) {
                            Log.d("OutfitViewModel", "✅ AI Image Ready: $generated")
                            return@withContext generated
                        } else {
                            Log.e("OutfitViewModel", "❌ COMPLETED status but no generated URL found.")
                            return@withContext null
                        }
                    } else if (status == "FAILED" || status == "ERROR") {
                        Log.e("OutfitViewModel", "❌ Freepik task FAILED. Response: $responseBody")
                        return@withContext null
                    }
                } catch (e: Exception) {
                    Log.e("OutfitViewModel", "Polling request exception: ${e.message}", e)
                    continue@pollingLoop
                }
            }

            Log.e("OutfitViewModel", "❌ Freepik task polling timed out")
            return@withContext null
        }

    private fun cleanUpTemporaryGarment(publicId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTemporaryGarment(publicId)
        }
    }

    fun getRecommendedOutfit(fullWardrobe: List<ClothingItem>): RecommendedOutfit {
        val profile = userProfile.value
        val isPersonal = recommendationType == "personal"

        if (!isPersonal && (_isGeneratingShoppingOutfit.value || _isGeneratingVTO.value)) {
            return RecommendedOutfit(
                topItem = null,
                bottomItem = null,
                recommendationText = "Generating AI look based on your profile...",
                isFromWardrobe = false
            )
        }

        if (!isPersonal && (_shoppingImageUrl.value != null || _vtoImageUrl.value != null)) {
            val url = _vtoImageUrl.value ?: _shoppingImageUrl.value
            if (url == null) return RecommendedOutfit(null, null, "Error retrieving image URL.", false)

            val shoppingText = if (cachedProfileUrl.isNullOrEmpty()) {
                "Your AI look shown in a standard flat lay."
            } else {
                "Your personalized AI try-on look has been generated!"
            }

            val generatedItem = ClothingItem(
                id = "AI_SHOP_${System.currentTimeMillis()}",
                imageUrl = url,
                category = "Full Outfit",
                uploadDate = System.currentTimeMillis()
            )

            return RecommendedOutfit(generatedItem, null, shoppingText, false)
        }

        if (isPersonal) {
            val availableTops = fullWardrobe.filter { it.category == "Top" }
            val availableBottoms = fullWardrobe.filter { it.category == "Bottom" }

            val topToRecommend = availableTops.firstOrNull()
            val bottomToRecommend = availableBottoms.firstOrNull()

            if (topToRecommend != null && bottomToRecommend != null) {
                val successText =
                    "Perfectly matched from your closet: A comfortable, ${selectedStyle} look for your ${selectedOccasion}!"

                return RecommendedOutfit(
                    topItem = topToRecommend,
                    bottomItem = bottomToRecommend,
                    recommendationText = successText,
                    isFromWardrobe = true
                )
            } else {
                return RecommendedOutfit(
                    topItem = null,
                    bottomItem = null,
                    recommendationText = "Could not find a complete outfit in your wardrobe. Try uploading more items.",
                    isFromWardrobe = false
                )
            }
        }

        return RecommendedOutfit(
            topItem = null,
            bottomItem = null,
            recommendationText = "Select your profile parameters and click 'Generate My Outfit'.",
            isFromWardrobe = false
        )
    }
}
