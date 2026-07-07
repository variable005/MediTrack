package com.example.meditrack.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meditrack.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    navController: NavController,
    viewModel: MainViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf<LocalTime?>(null) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var expiryDate by remember { mutableStateOf<LocalDate?>(null) }

    var hasAttemptedSave by remember { mutableStateOf(false) }

    val isNameValid = name.isNotBlank()
    val isDosageValid = dosage.isNotBlank()
    val isTimeValid = reminderTime != null
    val isStartValid = startDate != null
    val isEndValid = endDate != null
    val isExpiryValid = expiryDate != null

    val isFormValid = isNameValid && isDosageValid && isTimeValid && isStartValid && isEndValid && isExpiryValid

    // --- Dialog Logic ---
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context, { _, hour: Int, minute: Int -> reminderTime = LocalTime.of(hour, minute) },
        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
    )
    val startDatePickerDialog = DatePickerDialog(
        context, { _: DatePicker, year: Int, month: Int, day: Int -> startDate = LocalDate.of(year, month + 1, day) },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )
    val endDatePickerDialog = DatePickerDialog(
        context, { _: DatePicker, year: Int, month: Int, day: Int -> endDate = LocalDate.of(year, month + 1, day) },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )
    val expiryDatePickerDialog = DatePickerDialog(
        context, { _: DatePicker, year: Int, month: Int, day: Int -> expiryDate = LocalDate.of(year, month + 1, day) },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add New Medicine",
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    hasAttemptedSave = true
                    if (isFormValid) {
                        viewModel.addMedicine(
                            context = context,
                            name = name,
                            dosage = dosage,
                            reminderTime = reminderTime!!,
                            startDate = startDate!!,
                            endDate = endDate!!,
                            expiryDate = expiryDate!!
                        )
                        navController.popBackStack()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = FloatingActionButtonDefaults.largeShape
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save Medicine", modifier = Modifier.size(28.dp))
            }
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = scaffoldPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Card 1: Medicine Details ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Medicine Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isNameValid && hasAttemptedSave,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g., 500mg)*") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isDosageValid && hasAttemptedSave,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // --- Card 2: Schedule & Dates ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Schedule & Dates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ClickableField(
                        label = "Reminder Time*",
                        value = reminderTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Select time",
                        icon = Icons.Filled.Schedule,
                        onClick = { timePickerDialog.show() },
                        isError = !isTimeValid && hasAttemptedSave
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClickableField(
                                label = "Start Date*",
                                value = startDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Select",
                                icon = Icons.Filled.CalendarToday,
                                onClick = { startDatePickerDialog.show() },
                                isError = !isStartValid && hasAttemptedSave
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClickableField(
                                label = "End Date*",
                                value = endDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Select",
                                icon = Icons.Filled.CalendarToday,
                                onClick = { endDatePickerDialog.show() },
                                isError = !isEndValid && hasAttemptedSave
                            )
                        }
                    }

                    ClickableField(
                        label = "Expiry Date*",
                        value = expiryDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Select date",
                        icon = Icons.Filled.CalendarToday,
                        onClick = { expiryDatePickerDialog.show() },
                        isError = !isExpiryValid && hasAttemptedSave
                    )
                }
            }

            // Spacer to push content above the floating nav bar / bottom padding
            Spacer(Modifier.height(100.dp))
        }
    }
}

// --- Reusable ClickableField Composable ---
@Composable
fun ClickableField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isError: Boolean = false
) {
    val borderColor = if (isError) 
        MaterialTheme.colorScheme.error 
    else 
        MaterialTheme.colorScheme.outlineVariant
    
    val labelColor = if (isError) 
        MaterialTheme.colorScheme.error 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant
    
    val valueColor = if (value.startsWith("Select"))
        labelColor.copy(alpha = 0.6f)
    else
        MaterialTheme.colorScheme.onSurface

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                fontSize = 16.sp
            )
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        if (isError) {
            Text(
                text = "This field is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}