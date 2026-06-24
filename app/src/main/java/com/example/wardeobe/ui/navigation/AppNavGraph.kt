package com.example.wardeobe.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.net.Uri
import androidx.compose.ui.platform.LocalContext // Required for VTO Context access

import com.example.wardeobe.screens.HomeScreen
import com.example.wardeobe.screens.ProfileSetupScreen
import com.example.wardeobe.screens.SettingsScreen
import com.example.wardeobe.screens.UploadScreen
import com.example.wardeobe.screens.OutfitDisplayScreen
import com.example.wardeobe.screens.CameraScreen
import com.example.wardeobe.screens.ItemDetailScreen
import com.example.wardeobe.screens.AuthScreen
import com.example.wardeobe.screens.ProfileScreen
import com.example.wardeobe.screens.VtoGarmentSelectionScreen // REQUIRED IMPORT

import com.example.wardeobe.viewmodel.HomeViewModel
import com.example.wardeobe.viewmodel.OutfitViewModel
import com.example.wardeobe.viewmodel.UploadViewModel
import com.example.wardeobe.viewmodel.AuthViewModel
import com.example.wardeobe.viewmodel.ProfileViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggled: (Boolean) -> Unit
) {
    // 1. Instantiate ViewModels
    val uploadViewModel: UploadViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val homeViewModel: HomeViewModel = remember {
        HomeViewModel(uploadViewModel)
    }

    // FIX: OutfitViewModel needs BOTH dependencies
    val outfitViewModel: OutfitViewModel = remember {
        OutfitViewModel(
            profileViewModel = profileViewModel,
            uploadViewModel = uploadViewModel
        )
    }

    // Determine starting route based on login status
    val startRoute = if (authViewModel.isUserLoggedIn()) "home" else "auth"

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        // 🚨 Authentication Screen
        composable("auth") {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        // 🏠 Home Screen (Updated with VTO navigation)
        composable("home") {
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToUpload = { navController.navigate("upload?imageUri=null") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToCreateOutfit = { navController.navigate("profile_setup") },
                onNavigateToShopOutfit = { navController.navigate("outfit_display/shop") },
                onNavigateToCamera = { navController.navigate("camera_capture") },
                onNavigateToItemDetail = { itemId ->
                    val encodedId = Uri.encode(itemId)
                    navController.navigate("item_detail/$encodedId")
                },
                // Quick VTO Upload directs to the local selection flow
                onNavigateToVtoUpload = {
                    navController.navigate("select_garment_vto")
                }
            )
        }

        // 📸 Upload Screen (Legacy/Cloud Save)
        composable(
            route = "upload?imageUri={imageUri}",
            arguments = listOf(navArgument("imageUri") {
                nullable = true
                type = NavType.StringType
                defaultValue = null
            })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")

            UploadScreen(
                uploadViewModel = uploadViewModel,
                initialImageUri = imageUri,
                onBack = { navController.popBackStack() }
            )
        }

        // 🚨 VTO GARMENT SELECTION ROUTE (Local Flow Start)
        composable("select_garment_vto") {
            VtoGarmentSelectionScreen( // Use the dedicated selection screen
                viewModel = outfitViewModel,
                onBack = { navController.popBackStack() },
                // FIX: Navigate to the VTO display route, passing the local garment URI
                onNavigateToOutfitDisplay = { uri ->
                    val encodedUri = Uri.encode(uri.toString())
                    // Go to the VTO processing/display screen
                    navController.navigate("outfit_display_vto/$encodedUri") {
                        popUpTo("select_garment_vto") { inclusive = true }
                    }
                }
            )
        }

        // 🚨 VTO DISPLAY ROUTE (Receives local garment URI and triggers VTO)
        composable(
            route = "outfit_display_vto/{garmentUri}",
            arguments = listOf(navArgument("garmentUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val garmentUriString = backStackEntry.arguments?.getString("garmentUri")
            val context = LocalContext.current // 🌟 FIX: Get Context inside the Composable scope

            // Trigger VTO generation immediately using the local URI
            LaunchedEffect(garmentUriString) {
                if (!garmentUriString.isNullOrEmpty()) {
                    // 🌟 FIX: Pass the Context to the ViewModel function
                    outfitViewModel.startVtoLocalGeneration(context, Uri.parse(garmentUriString))
                }
            }

            // Display the OutfitDisplayScreen with the local VTO mode
            OutfitDisplayScreen(
                mode = "vto_local",
                viewModel = outfitViewModel,
                onBack = { navController.popBackStack() },
                homeViewModel = homeViewModel
            )
        }

        // Camera Capture Screen (Remains unchanged)
        composable("camera_capture") {
            CameraScreen(
                onCaptureComplete = { uri ->
                    if (uri != null) {
                        val encodedUri = Uri.encode(uri.toString())
                        navController.popBackStack()
                        navController.navigate("upload?imageUri=$encodedUri")
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        // Profile Picture/VTO Setup Screen Route (Remains unchanged)
        composable("profile_view") {
            ProfileScreen(
                profileViewModel = profileViewModel,
                uploadViewModel = uploadViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ⚙️ Settings Screen (Remains unchanged)
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                currentDarkModeState = isDarkTheme,
                onDarkModeToggled = onThemeToggled,
                onNavigateToProfileView = { navController.navigate("profile_view") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // 📝 User Profile Setup Screen (Remains unchanged)
        composable("profile_setup") {
            ProfileSetupScreen(
                viewModel = outfitViewModel,
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }

        // 🖼️ Item Detail Route Definition (Remains unchanged)
        composable(
            route = "item_detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            if (itemId != null) {
                ItemDetailScreen(
                    itemId = itemId,
                    homeViewModel = homeViewModel,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("Error: Item not found.")
            }
        }


        // 👗 Outfit Display/Recommendation Screen (Original route)
        composable("outfit_display/{mode}") { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "create"
            OutfitDisplayScreen(
                mode = mode,
                viewModel = outfitViewModel,
                onBack = { navController.popBackStack() },
                homeViewModel = homeViewModel
            )
        }
    }
}