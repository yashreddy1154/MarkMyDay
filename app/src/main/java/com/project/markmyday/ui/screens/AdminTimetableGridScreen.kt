package com.project.markmyday.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.data.model.Period
import com.project.markmyday.ui.utils.getSubjectColorForGrid
import com.project.markmyday.viewmodel.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTimetableGridScreen(
    className: String,
    onBack: () -> Unit,
    viewModel: TimetableViewModel = viewModel()
) {
    val timetables by viewModel.allTimetables.collectAsState()
    val currentTimetable = timetables.find { it.className == className }
    
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Class number check for limiting periods
    val classNum = className.split(" ").last().toIntOrNull() ?: 10
    val isPrimary = classNum in 1..5

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(className, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Main scrollable content
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Column {
                        // Day Labels
                        Row(
                            modifier = Modifier.padding(start = 100.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            days.forEach { day ->
                                Surface(
                                    modifier = Modifier.size(width = 120.dp, height = 40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(day.take(3), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        }

                        // Grid Content
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminTimetableRow(0, "08:30 AM", "09:00 AM", "Prayer", isSpecial = true, days, currentTimetable, horizontalScrollState.value)
                            AdminTimetableRow(1, "09:00 AM", "09:45 AM", "P-1", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                            AdminTimetableRow(2, "09:45 AM", "10:30 AM", "P-2", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                            AdminTimetableRow(0, "10:30 AM", "10:45 AM", "Short Break 1", isSpecial = true, days, currentTimetable, horizontalScrollState.value)
                            AdminTimetableRow(3, "10:45 AM", "11:30 AM", "P-3", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                            AdminTimetableRow(4, "11:30 AM", "12:15 PM", "P-4", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                            AdminTimetableRow(5, "12:15 PM", "01:00 PM", "P-5", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                            AdminTimetableRow(0, "01:00 PM", "02:00 PM", "Lunch", isSpecial = true, days, currentTimetable, horizontalScrollState.value)
                            
                            if (!isPrimary) {
                                AdminTimetableRow(6, "02:00 PM", "02:45 PM", "P-6", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                                AdminTimetableRow(7, "02:45 PM", "03:30 PM", "P-7", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                                AdminTimetableRow(0, "03:30 PM", "03:45 PM", "Short Break 2", isSpecial = true, days, currentTimetable, horizontalScrollState.value)
                                AdminTimetableRow(8, "03:45 PM", "04:30 PM", "P-8", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                                AdminTimetableRow(9, "04:30 PM", "05:15 PM", "P-9", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                                AdminTimetableRow(10, "05:15 PM", "05:45 PM", "P-10", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                            } else {
                                // For Primary, we only show up to P-7 (which is usually the end of day or needs specific mapping)
                                // User said "for classes 1 to 5 only till 7 periods should appear"
                                // Based on the standard 10 period structure, we show P-6 and P-7 then stop.
                                AdminTimetableRow(6, "02:00 PM", "02:45 PM", "P-6", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                                AdminTimetableRow(7, "02:45 PM", "03:30 PM", "P-7", isSpecial = false, days, currentTimetable, horizontalScrollState.value)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTimetableRow(
    periodNumber: Int,
    startTime: String,
    endTime: String,
    label: String,
    isSpecial: Boolean,
    days: List<String>,
    currentTimetable: com.project.markmyday.data.model.Timetable?,
    scrollValue: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Time Slot Info Card
        Card(
            modifier = Modifier.size(width = 88.dp, height = 80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSpecial) label.take(5) + ".." else label,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$startTime\n$endTime",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }
        }

        if (isSpecial) {
            // Special Spanned Tile with Floating/Sticky Text
            Card(
                modifier = Modifier.size(width = (120 * 6 + 12 * 5).dp, height = 80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Floating Text: Adjust padding based on scroll to keep it visible
                    // Total width is roughly 780dp. We want to keep it centered in the visible area.
                    // A simple way is to use padding that tracks the scrollValue.
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            // Approximate conversion of scroll value (pixels) to dp and adding a base padding
                            // 2.75 is a rough density factor for common devices, but we'll use a safer approach
                            .padding(start = (scrollValue / 3).dp + 16.dp)
                    )
                }
            }
        } else {
            // Regular Periods for each day
            days.forEach { day ->
                val period = currentTimetable?.weeklySchedule?.get(day)?.periods?.find { it.periodNumber == periodNumber }
                AdminPeriodCard(period)
            }
        }
    }
}

@Composable
fun AdminPeriodCard(period: Period?) {
    val isLeisure = period == null || period.subject.isEmpty()
    val subjectColor = if (!isLeisure && period != null) getSubjectColorForGrid(period.subject) else Color.DarkGray

    Card(
        modifier = Modifier.size(width = 120.dp, height = 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isLeisure) subjectColor.copy(alpha = 0.1f) else subjectColor.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, if (!isLeisure) subjectColor.copy(alpha = 0.5f) else subjectColor.copy(alpha = 0.2f))
    ) {
        if (!isLeisure && period != null) {
            Box(modifier = Modifier.padding(10.dp).fillMaxSize()) {
                Text(
                    text = period.subject,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = subjectColor,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                Text(
                    text = period.teacherName,
                    fontSize = 10.sp,
                    color = subjectColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomEnd),
                    maxLines = 1
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "-",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = subjectColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
