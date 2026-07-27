package com.example.wardeobe.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wardeobe.viewmodel.OutfitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VtoGarmentSelectionScreen(
    viewModel: OutfitViewModel,
    // Callback handles navigation after selection
    onNavigateToOutfitDisplay: (garmentUri: Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isLoading = viewModel.isGeneratingVTO.collectAsStateWithLifecycle()

    // 1. Gallery Launcher to pick the garment image
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Immediately start processing/navigate to display
            onNavigateToOutfitDisplay(uri)
        } else {
            Toast.makeText(context, "Garment selection cancelled.", Toast.LENGTH_SHORT).show()
            onBack() // Go back if selection failed
        }
    }

    // Launch the gallery picker immediately when the screen appears
    val hasLaunched = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(hasLaunched.value) {
        if (!hasLaunched.value) {
            galleryLauncher.launch("image/*")
            hasLaunched.value = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Garment for Try-On", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Show a loading or waiting screen while the gallery is open
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isLoading.value) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Preparing VTO...", color = MaterialTheme.colorScheme.onSurface)
                } else {
                    Text("Opening gallery to select garment...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Re-open Gallery")
                    }
                }
            }
        }
    }
}