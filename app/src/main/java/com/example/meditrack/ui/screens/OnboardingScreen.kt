package com.example.meditrack.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meditrack.navigation.BottomNavItem
import com.example.meditrack.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: MainViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val savedName by viewModel.userName.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    var nameInput by remember(savedName) { mutableStateOf(savedName) }
    var hasAttemptedSave by remember { mutableStateOf(false) }

    val isNameValid = nameInput.isNotBlank()

    fun triggerHaptic() {
        if (hapticEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (onboardingCompleted) "Edit Profile" else "Setup Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    if (onboardingCompleted) {
                        IconButton(
                            onClick = {
                                triggerHaptic()
                                navController.popBackStack()
                            }
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { topBarPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topBarPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Header (Clean, Centered, Symmetrical)
            if (!onboardingCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Medication,
                        contentDescription = "Welcome Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "Welcome to MediTrack",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Let's personalize your setup to get started on tracking your daily medications.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Card 1: User Profile Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile Name", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Profile Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name*") },
                        placeholder = { Text("e.g. John Doe") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isNameValid && hasAttemptedSave,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // Card 2: App Style & Theme Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Palette, contentDescription = "Theme Preferences", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Theme Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Theme Mode Selection (Symmetrical Pill Selector)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Theme Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        
                        SegmentedControl(
                            options = listOf(
                                "System" to "system",
                                "Light" to "light",
                                "Dark" to "dark",
                                "Time" to "time"
                            ),
                            selectedOption = themeMode,
                            onOptionSelected = {
                                viewModel.setThemeMode(it)
                                triggerHaptic()
                            }
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Theme Color Selection Row (Centered, Symmetrical Swatches)
                    val isTimeWise = themeMode == "time"
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Theme Color", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            if (isTimeWise) {
                                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                                val autoColorName = when (hour) {
                                    in 5..11 -> "Teal (Morning)"
                                    in 12..17 -> "Orange (Afternoon)"
                                    else -> "Purple (Night)"
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "($autoColorName)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val colorsList = listOf(
                                Triple("Teal", "teal", Color(0xFF00696B)),
                                Triple("Blue", "blue", Color(0xFF1976D2)),
                                Triple("Purple", "purple", Color(0xFF6750A4)),
                                Triple("Green", "green", Color(0xFF386A20)),
                                Triple("Orange", "orange", Color(0xFF8B5000))
                            )
                            val activeColorValue = if (isTimeWise) {
                                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                                when (hour) {
                                    in 5..11 -> "teal"
                                    in 12..17 -> "orange"
                                    else -> "purple"
                                }
                            } else {
                                themeColor
                            }

                            colorsList.forEach { (name, value, colorSample) ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            colorSample.copy(alpha = if (isTimeWise && activeColorValue != value) 0.3f else 1f),
                                            CircleShape
                                        )
                                        .clip(CircleShape)
                                        .border(
                                            width = if (activeColorValue == value) 3.dp else 1.dp,
                                            color = if (activeColorValue == value) {
                                                if (isTimeWise) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            } else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable(enabled = !isTimeWise) {
                                            viewModel.setThemeColor(value)
                                            triggerHaptic()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (activeColorValue == value) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = name,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: App Behavior Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Vibration, contentDescription = "Haptics", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column {
                            Text("Haptic Feedback", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Vibrate on interactions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = {
                            viewModel.setHapticEnabled(it)
                            if (it) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save / Get Started Button (Centered, Padded)
            Button(
                onClick = {
                    hasAttemptedSave = true
                    if (isNameValid) {
                        viewModel.setUserName(nameInput.trim())
                        triggerHaptic()
                        if (onboardingCompleted) {
                            navController.popBackStack()
                        } else {
                            viewModel.setOnboardingCompleted(true)
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (onboardingCompleted) "Save Changes" else "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Custom Symmetrical Pill Segmented Control Composable
@Composable
private fun <T> SegmentedControl(
    options: List<Pair<String, T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (label, value) ->
            val isSelected = selectedOption == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onOptionSelected(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
