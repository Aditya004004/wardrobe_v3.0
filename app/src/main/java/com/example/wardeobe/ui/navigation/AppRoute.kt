package com.example.wardeobe.ui.navigation

/**
 * Sealed class representing the navigation destinations in the app.
 * Using a sealed class provides type‑safety and exhaustiveness checks.
 */
sealed class AppRoute(val route: String) {
    object Auth : AppRoute("auth")
    object Home : AppRoute("home")
    object Settings : AppRoute("settings")
    object Upload : AppRoute("upload?imageUri={imageUri}")
    object ProfileSetup : AppRoute("profile_setup")
    object CameraCapture : AppRoute("camera_capture")
    object ProfileView : AppRoute("profile_view")
    object VtoGarmentSelection : AppRoute("select_garment_vto")
    object OutfitDisplayVto : AppRoute("outfit_display_vto/{garmentUri}")
    object OutfitDisplay : AppRoute("outfit_display/{mode}")
    object ItemDetail : AppRoute("item_detail/{itemId}")
}
