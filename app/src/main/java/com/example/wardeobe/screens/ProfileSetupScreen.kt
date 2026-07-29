package com.example.wardeobe.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.wardeobe.viewmodel.OutfitViewModel

import androidx.compose.material3.ChipColors
import androidx.compose.material3.AssistChipDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: OutfitViewModel = hiltViewModel(),
    onBack: () -> Unit,
    navController: NavHostController
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val selectedOccasion by viewModel.selectedOccasion.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val recommendationType by viewModel.recommendationType.collectAsStateWithLifecycle()

    val bodyTypes = listOf("H", "X", "Y", "O", "A")
    val ageGroups = listOf("<20", "21-24", "25-30", "31-35", "36-45", "45+")
    val heights = listOf("less than 5'2\"", "5'3\"-5'5\"", "5'6\"-5'8\"", "5'9\"-5'11\"", "6ft+")
    val occasions = listOf("Party", "Wedding", "Birthday", "Meeting", "Casual")
    val styles = listOf(
        "Formal/Professional",
        "Ethnic/Traditional",
        "Comfort",
        "Minimal",
        "Elegant",
        "Casual",
        "Bold",
        "Playful"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Define Your Style Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ... Gender, Body Type, Skin Tone, Age/Height sections unchanged ...

            // 1. Gender (Uses SegmentedButtonRow)
            item(key = "gender_section") {
                Text("1. Select Gender:", fontWeight = FontWeight.SemiBold)
                SegmentedButtonRow(
                    options = listOf("Male", "Female"),
                    selectedOption = profile.gender,
                    onOptionSelected = viewModel::updateGender
                )
            }

            // 2. Body Type (Uses HorizontalScrollSelector)
            item(key = "body_type_section") {
                Text("2. Select Body Type:", fontWeight = FontWeight.SemiBold)
                BodyTypeSelector(
                    bodyTypes = bodyTypes,
                    selectedType = profile.bodyType,
                    onTypeSelected = viewModel::updateBodyType
                )
            }

            // 3. Skin Tone (Uses SegmentedButtonRow)
            item(key = "skin_tone_section") {
                Text("3. Select Skin Tone:", fontWeight = FontWeight.SemiBold)
                SegmentedButtonRow(
                    options = listOf("Warm", "Cool"),
                    selectedOption = profile.skinTone,
                    onOptionSelected = viewModel::updateSkinTone
                )
            }

            item(key = "age_height_section") {
                Text("4. Age and Height:", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DropdownSelector(
                        label = "Age Group",
                        options = ageGroups,
                        selectedOption = profile.ageGroup,
                        onOptionSelected = viewModel::updateAgeGroup,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    DropdownSelector(
                        label = "Height",
                        options = heights,
                        selectedOption = profile.heightGroup,
                        onOptionSelected = viewModel::updateHeightGroup,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            }

            item(key = "params_header") {
                Text("5. Choose Outfit Parameters:", fontWeight = FontWeight.Bold)
            }

            // 5. Occasion
            item(key = "occasion_selector") {
                Text("Occasion:", fontWeight = FontWeight.Medium)
                HorizontalScrollSelector(
                    options = occasions,
                    selectedOption = selectedOccasion,
                    onOptionSelected = viewModel::updateSelectedOccasion
                )
            }

            // 6. Style
            item(key = "style_selector") {
                Text("Style:", fontWeight = FontWeight.Medium)
                HorizontalScrollSelector(
                    options = styles,
                    selectedOption = selectedStyle,
                    onOptionSelected = viewModel::updateSelectedStyle
                )
            }

            // 7. Outfit Source
            item(key = "source_selector") {
                Text("Outfit Source:", fontWeight = FontWeight.Medium)
                SegmentedButtonRow(
                    options = listOf("Personal Wardrobe", "Build New Style"),
                    selectedOption = if (recommendationType == "personal") "Personal Wardrobe" else "Build New Style",
                    onOptionSelected = { viewModel.updateRecommendationType(if (it == "Personal Wardrobe") "personal" else "shop") }
                )
            }

            item(key = "save_button") {
                Button(
                    onClick = {
                        if (profile.gender.isEmpty() ||
                            profile.bodyType.isEmpty() ||
                            profile.skinTone.isEmpty() ||
                            selectedOccasion.isEmpty() ||
                            selectedStyle.isEmpty()
                        ) {
                            Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Determine the route
                        val route = if (recommendationType == "personal") {
                            "outfit_display/create"
                        } else {
                            "outfit_display/shop"
                        }

                        // Clear old image state if we are going into "shop" mode
                        if (recommendationType == "shop") {
                            viewModel.resetShoppingOutfit()
                        }

                        navController.navigate(route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Generate My Outfit")
                }
            }
        }
    }
}
// --- Helper Composables (No changes needed, as the logic is in the screen now) ---

@Composable
fun SegmentedButtonRow(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Button(
                onClick = { onOptionSelected(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 2.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(option, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BodyTypeSelector(
    bodyTypes: List<String>,
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        bodyTypes.forEach { type ->
            val isSelected = type == selectedType
            AssistChip(
                onClick = { onTypeSelected(type) },
                label = { Text(type, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    if (isSelected) {
                        Icon(Icons.Default.Done, null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    leadingIconContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun HorizontalScrollSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            AssistChip(
                onClick = { onOptionSelected(option) },
                label = { Text(option, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    leadingIconContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        modifier = modifier.height(56.dp)
    ) {
        Text(selectedOption.ifEmpty { label }, maxLines = 1, modifier = Modifier.weight(1f))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 150.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}