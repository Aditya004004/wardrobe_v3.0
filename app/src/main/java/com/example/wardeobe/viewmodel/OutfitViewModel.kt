package com.example.wardeobe.viewmodel


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
import android.content.Context
import java.util.UUID

@HiltViewModel
class OutfitViewModel @Inject constructor(
    private val profileRepository: com.example.wardeobe.data.ProfileRepository,
    private val repository: com.example.wardeobe.data.WardrobeRepository,
    private val aiGenerationRepository: com.example.wardeobe.data.AiGenerationRepository,
    private val auth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {


    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _selectedOccasion = MutableStateFlow("")
    val selectedOccasion: StateFlow<String> = _selectedOccasion.asStateFlow()

    private val _selectedStyle = MutableStateFlow("")
    val selectedStyle: StateFlow<String> = _selectedStyle.asStateFlow()

    private val _recommendationType = MutableStateFlow("personal")
    val recommendationType: StateFlow<String> = _recommendationType.asStateFlow()

    fun updateSelectedOccasion(occasion: String) { _selectedOccasion.value = occasion }
    fun updateSelectedStyle(style: String) { _selectedStyle.value = style }
    fun updateRecommendationType(type: String) { _recommendationType.value = type }

    private val _shoppingImageUrl = MutableStateFlow<String?>(null)
    val shoppingImageUrl: StateFlow<String?> = _shoppingImageUrl.asStateFlow()
    private val _isGeneratingShoppingOutfit = MutableStateFlow(false)
    val isGeneratingShoppingOutfit: StateFlow<Boolean> = _isGeneratingShoppingOutfit.asStateFlow()

    private val _vtoImageUrl = MutableStateFlow<String?>(null)
    val vtoImageUrl: StateFlow<String?> = _vtoImageUrl.asStateFlow()
    private val _isGeneratingVTO = MutableStateFlow(false)
    val isGeneratingVTO: StateFlow<Boolean> = _isGeneratingVTO.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    private val _hasProfilePicture = MutableStateFlow(false)
    val hasProfilePicture: StateFlow<Boolean> = _hasProfilePicture.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                profileRepository.fetchProfilePictureUrl(uid)
            }
            profileRepository.profilePictureUrl.collect { url ->
                _hasProfilePicture.value = url.isNotEmpty()
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
        _errorMessage.value = null
        _isGeneratingVTO.value = true

        viewModelScope.launch {
            try {
                val profileUrl = profileRepository.profilePictureUrl.value.ifEmpty { null }

                val uploadResult = repository.uploadTemporaryGarment(context, garmentUri)
                val publicGarmentUrl = uploadResult?.get("secure_url") as? String
                val tempPublicId = uploadResult?.get("public_id") as? String

                if (publicGarmentUrl.isNullOrEmpty()) {
                    Log.e("OutfitViewModel", "Temporary garment upload failed.")
                    _isGeneratingVTO.value = false
                    _errorMessage.value = "Temporary garment upload failed."
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("OutfitViewModel", "Generation failed", e)
                _isGeneratingVTO.value = false
                _vtoImageUrl.value = null
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            }
        }
    }

    fun startShoppingOutfitGeneration() {
        if (_isGeneratingShoppingOutfit.value || _isGeneratingVTO.value) return

        resetShoppingOutfit()
        _errorMessage.value = null
        _isGeneratingShoppingOutfit.value = true

        viewModelScope.launch {
            try {
                val profileUrl = profileRepository.profilePictureUrl.value.ifEmpty { null }
                val shopUrl = generateNewOutfitWithAi()
                _shoppingImageUrl.value = shopUrl
                _isGeneratingShoppingOutfit.value = false
                if (!shopUrl.isNullOrEmpty() && !profileUrl.isNullOrEmpty()) {
                    startVtoGeneration(shopUrl, profileUrl)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("OutfitViewModel", "Generation failed", e)
                _isGeneratingShoppingOutfit.value = false
                _shoppingImageUrl.value = null
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            }
        }
    }

    private fun startVtoGeneration(shopImageUrl: String, profileUrl: String) {
        _isGeneratingVTO.value = true
        _vtoImageUrl.value = null

        viewModelScope.launch {
            try {
                val vtoUrl = generateVtoImage(shopImageUrl, profileUrl)
                _vtoImageUrl.value = vtoUrl
                _isGeneratingVTO.value = false
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("OutfitViewModel", "Generation failed", e)
                _isGeneratingVTO.value = false
                _vtoImageUrl.value = null
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            }
        }
    }

    private suspend fun initiateAiGeneration(prompt: String, referenceImages: JSONArray): String? {
        return aiGenerationRepository.generateImage(prompt, referenceImages)
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
            return@withContext initiateAiGeneration(prompt, referenceImages)
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
            return@withContext initiateAiGeneration(prompt, referenceImages)
        }

    private suspend fun generateNewOutfitWithAi(): String? =
        withContext(Dispatchers.IO) {
            val profile = userProfile.value

            val detailedPrompt = buildString {
                append("Generate a high-resolution, hyper-realistic image of a single, complete outfit in a perfectly lit, professional studio flat lay style. ")
                append("The outfit must be ${selectedStyle.value} and suitable for a ${selectedOccasion.value} event. ")
                append("Based on a ${profile.gender}, ${profile.bodyType} body type, and ${profile.skinTone} skin tone. ")
                append("Focus on clear separation of the pieces on a simple, neutral background. ")
                append("Render the fabrics with realistic texture and depth. Photorealistic, 8K quality.")
            }

            return@withContext initiateAiGeneration(detailedPrompt, JSONArray())
        }

    private fun cleanUpTemporaryGarment(publicId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTemporaryGarment(publicId)
        }
    }

    fun getRecommendedOutfit(fullWardrobe: List<ClothingItem>): RecommendedOutfit {
        val profile = userProfile.value
        val isPersonal = recommendationType.value == "personal"

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

            val shoppingText = if (_hasProfilePicture.value) {
                "Your personalized AI try-on look has been generated!"
            } else {
                "Your AI look shown in a standard flat lay."
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
