package com.example.wardeobe.screens

// 🌟 FIX: The correct import for statusBarsPadding() is from androidx.compose.foundation.layout
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.wardeobe.viewmodel.HomeViewModel
import java.util.concurrent.TimeUnit
import java.time.temporal.ChronoUnit // 🌟 New import for precise date calculation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit
) {
    // Collects the list of items from the ViewModel state
    val clothes by homeViewModel.wardrobeItems.collectAsStateWithLifecycle()

    // Find the specific item by ID. Recalculates if the ID or the list changes.
    val item = remember(itemId, clothes) {
        clothes.find { it.id == itemId }
    }
    val context = LocalContext.current

    // Calculate days ago (Now uses the correctly fetched uploadDate from ViewModel)
    val daysAgo = remember(item) {
        if (item == null) return@remember "N/A"

        val diffMillis = System.currentTimeMillis() - item.uploadDate

        // Use TimeUnit to convert milliseconds to days for accurate reporting
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        when (days) {
            0L -> "Today"
            1L -> "1 day ago"
            else -> "$days days ago"
        }
    }

    // Deletion logic wrapper
    fun deleteItem() {
        if (item != null) {
            homeViewModel.deleteClothingItem(item.id) { success ->
                if (success) {
                    Toast.makeText(context, "Item removed.", Toast.LENGTH_SHORT).show()
                    onBack() // Navigate back after successful deletion
                } else {
                    Toast.makeText(context, "Deletion failed. Check logs.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Item not found or recently deleted.", modifier = Modifier.offset(y = 50.dp))
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Full Image Display
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = item.category,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // TOP MANAGEMENT BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    // Semi-transparent background for readability
                    .background(Color.Black.copy(alpha = 0.3f))
                    .statusBarsPadding()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Back Button
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Center: Days Ago
                Text(
                    text = daysAgo,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)
                )

                // Right: Delete Button
                IconButton(onClick = ::deleteItem) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove Item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}