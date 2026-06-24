package com.example.wardeobe.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(onCaptureComplete: (Uri?) -> Unit) {
    val context = LocalContext.current
    val authority = context.packageName + ".fileprovider"

    // 1. State to hold the final captured image URI
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 2. State for the URI the camera will save to
    var cameraTargetUri by remember { mutableStateOf<Uri?>(null) }

    // Logic for handling the temporary file needed by the camera
    val tempFile = remember {
        File(context.cacheDir, "temp_image.jpg").apply {
            if (!exists()) createNewFile()
        }
    }

    // Function to generate the FileProvider URI
    val getTempUri: () -> Uri = {
        FileProvider.getUriForFile(context, authority, tempFile)
    }

    // 3. Camera Launch Contract - Defined early
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraTargetUri != null) {
            // Success: Set the captured image URI
            capturedImageUri = cameraTargetUri
        } else {
            Toast.makeText(context, "Photo capture cancelled or failed.", Toast.LENGTH_SHORT).show()
            capturedImageUri = null
        }
        // Always reset the camera target URI state after launch attempt
        cameraTargetUri = null
    }

    val launchCamera = { uri: Uri ->
        cameraTargetUri = uri
        cameraLauncher.launch(uri)
    }


    // 4. Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed to take the photo
            launchCamera(getTempUri())
        } else {
            Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    // Check initial permission state
    val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    // Function to handle button click: Check permission, then launch camera or request permission
    val handleCaptureClick: () -> Unit = {
        if (isCameraPermissionGranted) {
            launchCamera(getTempUri())
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture Item") },
                navigationIcon = {
                    IconButton(onClick = { onCaptureComplete(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Changed to Top to manually control spacing
        ) {
            // 🌟 FIX 1: Reduced Image Display Area Weight (e.g., weight 3f)
            Box(
                modifier = Modifier
                    .weight(3f) // Takes up less vertical space than the old weight(1f) structure
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(capturedImageUri),
                        contentDescription = "Captured Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Camera Prompt",
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // 🌟 FIX 2: Spacer to push content down (weight 1f, less than the bottom spacer)
            Spacer(modifier = Modifier.weight(1f))


            // --- Capture/Upload Buttons ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = handleCaptureClick,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                ) {
                    Text("Take Photo")
                }

                Button(
                    onClick = {
                        onCaptureComplete(capturedImageUri)
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    enabled = capturedImageUri != null,
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                ) {
                    Text("Use Photo")
                }
            }

            // 🌟 FIX 3: Spacer to occupy remaining space (weight 2f) and lift buttons further
            Spacer(modifier = Modifier.weight(2f))
        }
    }
}