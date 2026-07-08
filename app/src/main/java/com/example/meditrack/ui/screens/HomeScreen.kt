package com.example.meditrack.ui.screens

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import com.example.meditrack.data.Medicine
import com.example.meditrack.ui.theme.ExpiryGreen
import com.example.meditrack.ui.theme.ExpiryOrange
import com.example.meditrack.ui.theme.ExpiryRed
import com.example.meditrack.viewmodel.MainViewModel
import com.example.meditrack.viewmodel.TimeOfDay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import kotlin.random.Random

// (Health quotes, welcome message, and isTakenToday functions are unchanged)
// ...
private val healthQuotes = listOf(
    "The greatest wealth is health.",
    "Take care of your body. It's the only place you have to live.",
    "An ounce of prevention is worth a pound of cure.",
    "Consistency is key to achieving your health goals.",
    "Small steps every day lead to big results."
)
@Composable
private fun getWelcomeMessage(userName: String): String {
    val calendar = Calendar.getInstance()
    val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    return if (userName.isNotBlank()) "$greeting, $userName!" else "$greeting!"
}
private fun Medicine.isTakenToday(): Boolean {
    if (this.lastTakenTimestamp <= 0) return false
    val lastTakenDate = LocalDate.ofEpochDay(this.lastTakenTimestamp / (1000 * 60 * 60 * 24))
    return lastTakenDate == LocalDate.now()
}
// ...

@OptIn(ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val groupedMedicines by viewModel.groupedActiveMedicines.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val welcomeMessage = getWelcomeMessage(userName)
    val randomQuote = remember { healthQuotes.random(Random(System.currentTimeMillis())) }

    val hapticFeedback = LocalHapticFeedback.current
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()

    var medicineToDelete by remember { mutableStateOf<Medicine?>(null) }

    if (medicineToDelete != null) {
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            title = { Text("Delete Medication") },
            text = { Text("Are you sure you want to delete \"${medicineToDelete?.name}\"? This will permanently cancel all upcoming reminders.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        medicineToDelete?.let { viewModel.deleteMedicine(context, it) }
                        medicineToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        LaunchedEffect(Unit) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    val allMedicines = groupedMedicines.values.flatten()
    val totalDoses = allMedicines.size
    val takenDoses = allMedicines.count { it.isTakenToday() }
    val progress = if (totalDoses > 0) takenDoses.toFloat() / totalDoses.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    val (nextMedicine, isMissed) = remember(groupedMedicines, totalDoses, takenDoses) {
        val untakenMedicines = allMedicines
            .filter { !it.isTakenToday() }
            .sortedBy { it.reminderTime }

        val now = LocalTime.now()
        val upcoming = untakenMedicines.find { it.reminderTime.isAfter(now) }

        if (upcoming != null) {
            Pair(upcoming, false)
        } else if (untakenMedicines.isNotEmpty()) {
            Pair(untakenMedicines.last(), true)
        } else {
            Pair(null, false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        // --- 1. Daily Progress Header (Immersive Gradient) ---
        DailyProgressHeader(
            welcomeMessage = welcomeMessage,
            quote = randomQuote,
            takenDoses = takenDoses,
            totalDoses = totalDoses,
            progress = animatedProgress
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. "Next Up" Card ---
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        ) {
            NextUpCard(
                nextMedicine = nextMedicine,
                isMissed = isMissed,
                onMarkAsTaken = {
                    if (hapticEnabled) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    nextMedicine?.let { viewModel.markAsTaken(context, it) }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 3. Time Section Cards ---
        TimeOfDay.values().forEach { timeOfDay ->
            val medicinesInSection = groupedMedicines[timeOfDay] ?: emptyList()
            TimeSectionCard(
                timeOfDay = timeOfDay,
                medicines = medicinesInSection,
                onMarkAsTaken = { medicine ->
                    if (hapticEnabled) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    viewModel.markAsTaken(context, medicine)
                },
                onDelete = { medicine ->
                    if (hapticEnabled) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    medicineToDelete = medicine
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom space to let items scroll past the floating navigation bar
        Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 24.dp))
    }
}

// --- Daily Progress Header Composable ---
@Composable
fun DailyProgressHeader(
    welcomeMessage: String,
    quote: String,
    takenDoses: Int,
    totalDoses: Int,
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = welcomeMessage,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(88.dp)
            ) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.matchParentSize(),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.matchParentSize(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$takenDoses/$totalDoses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Taken",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// --- Next Up Card ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NextUpCard(
    nextMedicine: Medicine?,
    isMissed: Boolean,
    onMarkAsTaken: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMissed) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) 
            else 
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            width = 1.dp,
            color = if (isMissed) 
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f) 
            else 
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        )
    ) {
        AnimatedContent(
            targetState = nextMedicine,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) +
                        slideInVertically(animationSpec = tween(300, 100), initialOffsetY = { it / 2 }))
                    .with(fadeOut(animationSpec = tween(200)))
            }, label = "NextUpCardContent"
        ) { medicine ->
            if (medicine == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EventAvailable,
                        contentDescription = "All completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "All doses complete for today!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
                val icon = if (isMissed) Icons.Filled.Warning else Icons.Filled.Upcoming
                val label = if (isMissed) "MISSED DOSE" else "NEXT UP SCHEDULED"
                val labelColor = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                val containerColor = if (isMissed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(containerColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = labelColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = labelColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = medicine.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Medication,
                                contentDescription = "Dosage",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = medicine.dosage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = "Time",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = medicine.reminderTime.format(timeFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onMarkAsTaken,
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Mark as Taken",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- Time Section Card ---
@Composable
fun TimeSectionCard(
    timeOfDay: TimeOfDay,
    medicines: List<Medicine>,
    onMarkAsTaken: (Medicine) -> Unit,
    onDelete: (Medicine) -> Unit
) {
    val sectionIcon = when (timeOfDay) {
        TimeOfDay.Morning -> Icons.Filled.WbSunny
        TimeOfDay.Afternoon -> Icons.Filled.LightMode
        TimeOfDay.Night -> Icons.Filled.Bedtime
    }

    val sectionColor = when (timeOfDay) {
        TimeOfDay.Morning -> Color(0xFFE65100) // Warm orange
        TimeOfDay.Afternoon -> Color(0xFFFBC02D) // Sun yellow
        TimeOfDay.Night -> Color(0xFF1E88E5) // Blue night
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = sectionIcon,
                    contentDescription = timeOfDay.name,
                    tint = sectionColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeOfDay.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${medicines.count { it.isTakenToday() }}/${medicines.size} Done",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (medicines.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = "All clear",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Nothing scheduled here!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                medicines.forEachIndexed { index, medicine ->
                    MedicineItem(
                        medicine = medicine,
                        onMarkAsTaken = { onMarkAsTaken(medicine) },
                        onDelete = { onDelete(medicine) },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    if (index < medicines.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineItem(
    medicine: Medicine,
    onMarkAsTaken: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val isTakenToday = medicine.isTakenToday()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = medicine.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isTakenToday) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
                else 
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isTakenToday) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Medication,
                    contentDescription = "Dosage",
                    modifier = Modifier.size(14.dp),
                    tint = if (isTakenToday) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = medicine.dosage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isTakenToday) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = "Time",
                    modifier = Modifier.size(14.dp),
                    tint = if (isTakenToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = medicine.reminderTime.format(timeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isTakenToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                )
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Medicine",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = onMarkAsTaken,
            enabled = !isTakenToday,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = ExpiryGreen
            )
        ) {
            Icon(
                imageVector = if (isTakenToday) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (isTakenToday) "Taken" else "Mark as Taken",
                modifier = Modifier.size(28.dp),
                tint = if (isTakenToday) ExpiryGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryChip(expiryStatus: ExpiryStatus) {
    AssistChip(
        onClick = { /* No action needed */ },
        label = {
            Text(
                text = expiryStatus.status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = expiryStatus.color.copy(alpha = 0.15f),
            labelColor = expiryStatus.color
        ),
        border = BorderStroke(
            width = 1.dp,
            color = expiryStatus.color.copy(alpha = 0.5f)
        ),
        modifier = Modifier.height(24.dp)
    )
}

data class ExpiryStatus(val status: String, val color: Color)
fun getExpiryStatus(expiryDate: LocalDate): ExpiryStatus {
    val today = LocalDate.now()
    // FIX: Access property correctly
    val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate)

    return when {
        daysUntilExpiry < 0 -> ExpiryStatus("Expired", ExpiryRed)
        daysUntilExpiry <= 7 -> ExpiryStatus("Expires soon", ExpiryOrange)
        else -> ExpiryStatus("Valid", ExpiryGreen)
    }
}