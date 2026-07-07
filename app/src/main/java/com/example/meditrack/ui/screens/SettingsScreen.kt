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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meditrack.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    val hapticFeedback = LocalHapticFeedback.current
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()

    // Helper function to play haptic vibration
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
                        "Settings",
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
                    IconButton(
                        onClick = {
                            triggerHaptic()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Section 1: Preferences ---
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
                    Text(
                        text = "App Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Haptic Feedback Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Theme Selection Block
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Palette, contentDescription = "Theme", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Theme Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val themes = listOf("System" to "system", "Light" to "light", "Dark" to "dark")
                            themes.forEach { (label, value) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.setThemeMode(value)
                                            triggerHaptic()
                                        }
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = themeMode == value,
                                        onClick = {
                                            viewModel.setThemeMode(value)
                                            triggerHaptic()
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                }
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Text Size Block
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.TextFormat, contentDescription = "Text Size", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Text Size", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val sizes = listOf("Small" to "small", "Medium" to "medium", "Large" to "large")
                            sizes.forEach { (label, value) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.setTextSize(value)
                                            triggerHaptic()
                                        }
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = textSize == value,
                                        onClick = {
                                            viewModel.setTextSize(value)
                                            triggerHaptic()
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                }
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Color Theme Block (Dynamic color tweaking)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Palette, contentDescription = "Theme Color", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Theme Color", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                        ) {
                            val colorsList = listOf(
                                Triple("Teal", "teal", Color(0xFF00696B)),
                                Triple("Blue", "blue", Color(0xFF1976D2)),
                                Triple("Purple", "purple", Color(0xFF6750A4)),
                                Triple("Green", "green", Color(0xFF386A20)),
                                Triple("Orange", "orange", Color(0xFF8B5000))
                            )
                            colorsList.forEach { (name, value, colorSample) ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(colorSample, CircleShape)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (themeColor == value) 3.dp else 1.dp,
                                            color = if (themeColor == value) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.setThemeColor(value)
                                            triggerHaptic()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (themeColor == value) {
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

            // --- Section 2: About Application (Humorous) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = "About", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "About Application",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "\"We remind you so your mom doesn't have to\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "Because let's face it: you can remember all the lyrics to that obscure pop song from 2015, but you will 100% forget if you took your blue pill 20 minutes ago. MediTrack is here to save you from your own short-term memory memory leaks. You're welcome.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Section 3: Developer Section ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Code, contentDescription = "Remember Board", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Remember",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "\"Normal is just an agreement between yesterday and today.\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "a project by variable005",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Safety space for gestural navigators/bottom area
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
