package com.example.wardeobe.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.wardeobe.R
import com.example.wardeobe.viewmodel.UploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    uploadViewModel: UploadViewModel = viewModel(),
    initialImageUri: String?, // Receives URI string from navigation
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by uploadViewModel.uiState.collectAsStateWithLifecycle()

    var selectedImageUri by rememberSaveable { mutableStateOf(initialImageUri?.let { Uri.parse(it) } ?: null) }
    var selectedCategory by rememberSaveable { mutableStateOf("Top") } // Default Category
    var categoryMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // 🔸 Image picker launcher (if user chooses Gallery later)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // 🔸 Show Toast messages for final success/failure
    LaunchedEffect(uiState.userMessage) {
        if (uiState.userMessage.isNotEmpty() && !uiState.loading) {
            Toast.makeText(context, uiState.userMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Your Outfit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 📸 Image Display Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(
                            Color.LightGray.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        // Priority to the selected image
                        selectedImageUri != null -> Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        uiState.imageUrl != null -> Image(
                            painter = rememberAsyncImagePainter(uiState.imageUrl),
                            contentDescription = "AI Processed Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        else -> Image(
                            painter = painterResource(id = R.drawable.ic_gallery_placeholder),
                            contentDescription = "Placeholder",
                            modifier = Modifier.size(96.dp)
                        )
                    }

                    if (uiState.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🌟 CATEGORY DROPDOWN
                OutlinedButton(
                    onClick = { categoryMenuExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Category: ${
                            selectedCategory.ifEmpty { "Select Category" }
                        }",
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select Category"
                    )
                }

                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📤 Upload button (uses category)
                Button(
                    onClick = {
                        if (selectedImageUri != null && selectedCategory.isNotEmpty()) {
                            uploadViewModel.uploadImageWithAI(
                                context = context,
                                uri = selectedImageUri!!,
                                category = selectedCategory, // 🌟 PASS CATEGORY
                                onUploadComplete = onBack
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Please select an image and category!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedImageUri != null && !uiState.loading
                ) {
                    Text(
                        if (uiState.loading) uiState.userMessage else "Upload & Generate",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🖼️ Pick image button
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Choose from Gallery")
                }
            }
        }
    }
}
