package com.example.wardeobe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.wardeobe.model.RecommendedOutfit
import com.example.wardeobe.viewmodel.HomeViewModel
import com.example.wardeobe.viewmodel.OutfitViewModel
import com.example.wardeobe.viewmodel.ProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitDisplayScreen(
    mode: String,
    viewModel: OutfitViewModel,
    onBack: () -> Unit,
    homeViewModel: HomeViewModel
) {
    // Collect all states for dynamic UI updates
    val fullWardrobe by homeViewModel.wardrobeItems.collectAsState()
    val isGeneratingShop by viewModel.isGeneratingShoppingOutfit.collectAsState()
    val shopImageUrl by viewModel.shoppingImageUrl.collectAsState()

    // 🌟 VTO STATES
    val isGeneratingVTO by viewModel.isGeneratingVTO.collectAsState()
    val vtoImageUrl by viewModel.vtoImageUrl.collectAsState()

    // Flag for VTO mode (shop mode includes the original shop logic, vto_local is the quick upload)
    val isShopMode = mode == "shop" || mode == "vto_local"

    // Check if VTO profile pic exists via the injected ProfileViewModel
    val hasProfilePic = !viewModel.profileViewModel.uiState.collectAsState().value.profilePictureUrl.isEmpty()

    // Note: RecommendedOutfit logic is primarily for personal wardrobe matching
    val recommendedOutfit = remember(fullWardrobe) {
        viewModel.getRecommendedOutfit(fullWardrobe)
    }

    // LAUNCH EFFECT: Trigger base AI generation if needed (only for standard 'shop' mode)
    LaunchedEffect(mode) {
        if (mode == "shop" && shopImageUrl == null && !isGeneratingShop) {
            if (viewModel.selectedOccasion.isNotEmpty() && viewModel.selectedStyle.isNotEmpty()) {
                viewModel.startShoppingOutfitGeneration()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isShopMode) "AI Outfit Generation" else "Your Recommended Outfit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Make screen scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- 1. SHOP YOUR LOOK CARD (Base Garment) ---
            if (isShopMode && mode != "vto_local") { // Display the flat lay for standard shop generation
                AIGenerationCard(
                    title = "Shop Look (Garment Flat Lay)",
                    imageUrl = shopImageUrl,
                    isGenerating = isGeneratingShop,
                    recommendationText = recommendedOutfit.recommendationText
                )
            }

            // 🌟 2. VTO CARD (Virtual Try-On on User's Image or Quick VTO Result)
            if (isShopMode && hasProfilePic) {
                Spacer(modifier = Modifier.height(24.dp))
                AIGenerationCard(
                    title = if (mode == "vto_local") "Quick Try-On Result" else "Virtual Try-On (VTO)",
                    imageUrl = vtoImageUrl, // Use the VTO URL
                    isGenerating = isGeneratingVTO || (mode == "shop" && isGeneratingShop), // Show loading if merge or base gen is running
                    recommendationText = if (vtoImageUrl != null) "Your personalized try-on result." else "Merging garment onto your profile picture..."
                )
            } else if (isShopMode && !hasProfilePic && mode == "vto_local") {
                // Display the primary garment for local VTO fallback
                AIGenerationCard(
                    title = "Mannequin Try-On Result",
                    imageUrl = vtoImageUrl,
                    isGenerating = isGeneratingVTO,
                    recommendationText = if (vtoImageUrl != null) "Mannequin view is ready." else "Generating mannequin view..."
                )
            } else if (isShopMode && !hasProfilePic) {
                // Display warning if in standard shop mode and VTO profile is missing
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Upload a profile picture in Settings to enable Virtual Try-On.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 🌟 Helper Composable for displaying AI result cards
@Composable
fun AIGenerationCard(
    title: String,
    imageUrl: String?,
    isGenerating: Boolean,
    recommendationText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isGenerating -> {
                        CircularProgressIndicator()
                        Text("Processing...", modifier = Modifier.padding(top = 50.dp), color = Color.Gray)
                    }
                    imageUrl != null && imageUrl.startsWith("MOCKED_") -> {
                        // Display a mock placeholder if the image is the mock result
                        Text("MOCK AI RESULT. Waiting for real API call...", color = MaterialTheme.colorScheme.primary)
                    }
                    imageUrl != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        Text("No image generated.", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recommendationText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}