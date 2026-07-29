package com.example.wardeobe.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wardeobe.R
import com.example.wardeobe.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    currentDarkModeState: Boolean,
    onDarkModeToggled: (Boolean) -> Unit,
    onNavigateToProfileView: () -> Unit,
    // 🌟 FIX 1: Add new navigation callback for logging out
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Preferences",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            // 🌟 VTO Profile Upload Card
            item {
                SettingCard(
                    title = "Virtual Try-On Profile",
                    description = "Upload or update your profile picture for VTO",
                    onClick = onNavigateToProfileView,
                    trailing = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile_placeholder),
                            contentDescription = "Profile"
                        )
                    }
                )
            }

            // 🌙 Dark Mode Toggle
            item {
                SettingCard(
                    title = "Dark Mode",
                    description = "Switch between light and dark themes",
                    onClick = { onDarkModeToggled(!currentDarkModeState) },
                    trailing = {
                        Switch(
                            checked = currentDarkModeState,
                            onCheckedChange = onDarkModeToggled
                        )
                    }
                )
            }

            // 🧹 Clear Wardrobe
            item {
                SettingCard(
                    title = "Clear Wardrobe",
                    description = "Remove all uploaded clothing items (Coming Soon)",
                    onClick = null, // Disabled until feature is implemented
                    trailing = {
                        TextButton(onClick = { /* No action */ }, enabled = false) {
                            Text("Coming Soon", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }

            // 📤 Manage Cloud Storage
            item {
                SettingCard(
                    title = "Manage Cloudinary Storage",
                    description = "Check your uploaded images online",
                    onClick = { /* TODO: open Cloudinary link */ },
                    trailing = {
                        IconButton(onClick = { /* TODO: open Cloudinary link */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_fashion_placeholder),
                                contentDescription = "Cloud"
                            )
                        }
                    }
                )
            }

            // ℹ️ About
            item {
                SettingCard(
                    title = "About",
                    description = "Wardrobe v${BuildConfig.VERSION_NAME} — Your digital closet assistant"
                )
            }

            // 🌟 FIX 3: NEW LOGOUT BUTTON
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingCard(
                    title = "Log Out",
                    description = "Sign out of your current account.",
                    onClick = onLogout, // 🌟 Call the new logout action
                    trailing = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout_placeholder), // Assuming a new logout icon
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            trailing?.invoke()
        }
    }
}