package com.project.markmyday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.R
import com.project.markmyday.data.model.LeaveRequest
import com.project.markmyday.ui.components.DashboardTopBar
import com.project.markmyday.viewmodel.LeaveSubmissionState
import com.project.markmyday.viewmodel.LeaveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(
    onBack: () -> Unit,
    viewModel: LeaveViewModel = viewModel()
) {
    val leaveHistory by viewModel.leaveHistory.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()
    
    val dateRangePickerState = rememberDateRangePickerState()
    var reason by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val categories = listOf("Personal", "Emergency", "Hospital/Medical", "Function/Marriage", "Others")
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    LaunchedEffect(submissionState) {
        if (submissionState is LeaveSubmissionState.Success) {
            reason = ""
            dateRangePickerState.setSelection(null, null)
            viewModel.resetSubmissionState()
        }
    }

    if (showDatePicker) {
        DateRangePickerDialog(
            state = dateRangePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = { showDatePicker = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                title = "Apply for Leave",
                icon = Icons.Default.EditNote,
                onNotificationClick = { /* TODO */ }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Form Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            "New Request", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Category Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Leave Category",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    onClick = { showCategoryMenu = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(text = selectedCategory, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                        }
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = showCategoryMenu,
                                    onDismissRequest = { showCategoryMenu = false },
                                    modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surface)
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category, style = MaterialTheme.typography.bodyLarge) },
                                            onClick = {
                                                selectedCategory = category
                                                showCategoryMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Date Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Duration",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    val start = dateRangePickerState.selectedStartDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Start"
                                    val end = dateRangePickerState.selectedEndDateMillis?.let { dateFormatter.format(Date(it)) } ?: ""
                                    Text(
                                        text = if (dateRangePickerState.selectedStartDateMillis != null) {
                                            if (dateRangePickerState.selectedEndDateMillis != null) "$start - $end" else "$start (Single Day)"
                                        } else "Select Dates",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Reason",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                placeholder = { Text("Briefly explain your reason...") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Button(
                            onClick = {
                                val start = dateRangePickerState.selectedStartDateMillis
                                val end = dateRangePickerState.selectedEndDateMillis
                                if (start != null) {
                                    viewModel.applyLeave(
                                        startDate = Date(start),
                                        endDate = end?.let { Date(it) },
                                        reason = reason,
                                        category = selectedCategory
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            enabled = submissionState !is LeaveSubmissionState.Loading && dateRangePickerState.selectedStartDateMillis != null,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        ) {
                            if (submissionState is LeaveSubmissionState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Submit Request", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        if (submissionState is LeaveSubmissionState.Error) {
                            Text(
                                text = (submissionState as LeaveSubmissionState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            // History Section Header
            item {
                Text(
                    text = "Request History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // History Items
            if (leaveHistory.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No past requests found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(leaveHistory, key = { it.id }) { request ->
                    LeaveHistoryCard(request = request)
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(32.dp)
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.height(450.dp),
            title = { Text("Select leave dates", modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), fontWeight = FontWeight.Bold) },
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
fun LeaveHistoryCard(request: LeaveRequest) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val statusColor = when (request.status.lowercase()) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> MaterialTheme.colorScheme.error
        else -> Color(0xFFFF9800) // Pending
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                val start = request.startDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
                val end = request.endDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
                val dateRange = if (start == end) start else "$start - $end"
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateRange,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = request.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = request.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = request.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            if (request.status.lowercase() == "rejected" && request.rejectionReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin Note",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = request.rejectionReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
