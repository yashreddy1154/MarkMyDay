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
                            "New Leave Request 📝", 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Category Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "What's the reason? ✨",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    onClick = { showCategoryMenu = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Category, 
                                                contentDescription = null, 
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = selectedCategory, 
                                                style = MaterialTheme.typography.bodyLarge, 
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                DropdownMenu(
                                    expanded = showCategoryMenu,
                                    onDismissRequest = { showCategoryMenu = false },
                                    modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surface)
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) },
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
                                "When are you taking leave? 📅",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    val start = dateRangePickerState.selectedStartDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Start"
                                    val end = dateRangePickerState.selectedEndDateMillis?.let { dateFormatter.format(Date(it)) } ?: ""
                                    Text(
                                        text = if (dateRangePickerState.selectedStartDateMillis != null) {
                                            if (dateRangePickerState.selectedEndDateMillis != null) "$start - $end" else "$start (One Day)"
                                        } else "Pick your dates!",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Tell us more! 💬",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                placeholder = { Text("Write your reason here...") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
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
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            enabled = submissionState !is LeaveSubmissionState.Loading && dateRangePickerState.selectedStartDateMillis != null,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            if (submissionState is LeaveSubmissionState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                            } else {
                                Text("Send Request 🚀", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
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
    val (statusColor, statusIcon, statusText) = when (request.status.lowercase()) {
        "approved" -> Triple(Color(0xFF4CAF50), "✅ Approved!", "Great! Your leave is ready.")
        "rejected" -> Triple(MaterialTheme.colorScheme.error, "❌ Rejected", "Oh no, let's try again.")
        else -> Triple(Color(0xFFFF9800), "⏳ Pending...", "Wait for it! We're checking.") // Pending
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, statusColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val start = request.startDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
                val end = request.endDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
                val dateRange = if (start == end) start else "$start - $end"
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateRange,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🏷️ ${request.category}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = statusIcon,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Reason: ${request.reason}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )

            if (request.status.lowercase() == "rejected" && request.rejectionReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin Note 📝",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = request.rejectionReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
