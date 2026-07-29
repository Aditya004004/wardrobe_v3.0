package com.example.wardeobe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.wardeobe.R
import com.example.wardeobe.model.ClothingItem
import com.example.wardeobe.util.thumbnailUrl
import com.example.wardeobe.viewmodel.HomeViewModel
import com.example.wardeobe.viewmodel.WardrobeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToUpload: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateOutfit: () -> Unit,
    onNavigateToShopOutfit: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToItemDetail: (String) -> Unit,
    // 🌟 FIX 3: Added callback for the new VTO Upload flow
    onNavigateToVtoUpload: () -> Unit
) {
    // 🌟 COLLECT NEW STATES
    val filteredClothes by homeViewModel.filteredWardrobeItems.collectAsStateWithLifecycle()
    val selectedCategory by homeViewModel.selectedCategory.collectAsStateWithLifecycle()
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("home") }
    var menuExpanded by remember { mutableStateOf(false) }

    val categories = listOf("All", "Top", "Bottom", "Outerwear", "Shoes", "Accessory")
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        homeViewModel.fetchImages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WARDROBE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fashion_placeholder),
                            contentDescription = "Settings Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Outfit Menu"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Create an Outfit") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToCreateOutfit()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Shop an Outfit") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToShopOutfit()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Quick VTO Upload") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToVtoUpload()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToUpload() },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                NavigationBarItem(
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == "upload",
                    onClick = {
                        selectedTab = "upload"
                        onNavigateToUpload()
                    },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_fashion_placeholder),
                            contentDescription = "Upload"
                        )
                    },
                    label = { Text("Upload") }
                )

                NavigationBarItem(
                    selected = selectedTab == "camera",
                    onClick = {
                        selectedTab = "camera"
                        onNavigateToCamera()
                    },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_camera_placeholder),
                            contentDescription = "Camera"
                        )
                    },
                    label = { Text("Camera") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 🌟 NEW: Horizontal Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { homeViewModel.setFilterCategory(category) },
                        label = { Text(category) }
                    )
                }
            }

            // UI based on uiState
            when (uiState) {
                is WardrobeUiState.Loading -> {
                    WardrobeGridSkeleton()
                }
                is WardrobeUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 100.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clothes_placeholder),
                            contentDescription = "Empty Wardrobe",
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Your digital closet is empty!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the '+' button to capture and digitize your first item.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is WardrobeUiState.Error -> {
                    val message = (uiState as WardrobeUiState.Error).message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error: $message", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { homeViewModel.fetchImages() }) {
                            Text("Retry")
                        }
                    }
                }
                is WardrobeUiState.Success -> {
                    if (filteredClothes.isEmpty()) {
                        Text("No items match the selected filter.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filteredClothes, key = { it.id }) { item ->
                                ClothingCard(item = item, onClick = { onNavigateToItemDetail(item.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClothingCard(
    item: ClothingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(item.thumbnailUrl()),
                contentDescription = item.category,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
