package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.data.model.LeaveRequest
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
    var showDatePicker by remember { mutableStateOf(false) }

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
        topBar = {
            TopAppBar(
                title = { Text("Apply for Leave", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("New Request", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        // Date Selection
                        OutlinedCard(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                val start = dateRangePickerState.selectedStartDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Start"
                                val end = dateRangePickerState.selectedEndDateMillis?.let { dateFormatter.format(Date(it)) } ?: "End"
                                Text(
                                    text = if (dateRangePickerState.selectedStartDateMillis != null) "$start - $end" else "Select Date Range",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Reason for leave") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            minLines = 3
                        )

                        Button(
                            onClick = {
                                val start = dateRangePickerState.selectedStartDateMillis
                                val end = dateRangePickerState.selectedEndDateMillis
                                if (start != null && end != null) {
                                    viewModel.applyLeave(Date(start), Date(end), reason)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = submissionState !is LeaveSubmissionState.Loading && dateRangePickerState.selectedStartDateMillis != null,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (submissionState is LeaveSubmissionState.Loading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Submit Request", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (submissionState is LeaveSubmissionState.Error) {
                            Text(
                                text = (submissionState as LeaveSubmissionState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // History Section Header
            item {
                Text(
                    text = "Request History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.height(400.dp),
            title = { Text("Select leave dates", modifier = Modifier.padding(16.dp)) },
            showModeToggle = false
        )
    }
}

@Composable
fun LeaveHistoryCard(request: LeaveRequest) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val statusColor = when (request.status) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> MaterialTheme.colorScheme.error
        else -> Color(0xFFFFA000) // Pending
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val start = request.startDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
                val end = request.endDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
                Text(
                    text = "$start - $end",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = request.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
