package com.project.markmyday.ui.screens

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.R
import com.project.markmyday.data.model.DaySchedule
import com.project.markmyday.data.model.Period
import com.project.markmyday.data.model.SubjectQuota
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.TimetableViewModel

@Composable
fun getSubjectColorForGrid(subject: String): Color {
    val telugu = stringResource(R.string.subject_telugu_label).lowercase()
    val hindi = stringResource(R.string.subject_hindi_label).lowercase()
    val english = stringResource(R.string.subject_english_label).lowercase()
    val math = stringResource(R.string.subject_math_label).lowercase()
    val physics = stringResource(R.string.subject_physics_label).lowercase()
    val biology = stringResource(R.string.subject_biology_label).lowercase()
    val science = stringResource(R.string.subject_science_label).lowercase()
    val social = stringResource(R.string.subject_social_label).lowercase()
    val computer = stringResource(R.string.subject_computer_label).lowercase()

    return when (subject.lowercase()) {
        telugu -> Color(0xFF1E88E5) // Blue
        math -> Color(0xFF9E9E9E) // Grey
        physics -> Color(0xFF03A9F4) // Light Blue
        biology -> Color(0xFF4CAF50) // Green
        science -> Color(0xFF26A69A) // Mix of Blue and Green (Teal)
        social -> Color(0xFFFF9800) // Orange
        hindi -> Color(0xFFE91E63) // Pink/Custom
        english -> Color(0xFF673AB7) // Deep Purple/Custom
        computer -> Color(0xFF212121) // Black
        else -> Color(0xFF6200EE) // Default Purple
    }
}

class EditTimetableActivity : AppCompatActivity() {
    
    private val viewModel: TimetableViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val className = intent.getStringExtra("className") ?: ""

        setContent {
            MarkMyDayTheme {
                EditTimetableContent(
                    className = className, 
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimetableContent(
    className: String, 
    viewModel: TimetableViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val timetables by viewModel.allTimetables.collectAsState()
    
    val currentTimetable = timetables.find { it.className == className }
    val quotas = currentTimetable?.weeklyQuota ?: emptyMap()
    val classNum = className.split(" ").last().toIntOrNull() ?: 0
    val isPrimary = classNum in 1..5
    val periodCount = if (isPrimary) 7 else 10
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    // Local state for the grid
    var localSchedule by remember(currentTimetable) {
        val initialMap = days.associateWith { day ->
            currentTimetable?.weeklySchedule?.get(day) ?: DaySchedule()
        }.toMutableMap()
        
        // Auto-allocate Home Teacher for Period 1
        val homeTeacherId = currentTimetable?.homeTeacherId ?: ""
        val homeTeacherName = currentTimetable?.homeTeacherName ?: ""
        if (homeTeacherId.isNotEmpty()) {
            val homeTeacherSubject = quotas.values.find { it.teacherId == homeTeacherId }?.subject ?: ""
            if (homeTeacherSubject.isNotEmpty()) {
                days.forEach { day ->
                    val daySched = initialMap[day] ?: DaySchedule()
                    val periods = daySched.periods.toMutableList()
                    val p1 = periods.find { it.periodNumber == 1 }
                    val startTime = getStartTime(1)
                    val endTime = getEndTime(1)
                    
                    if (p1 == null) {
                        periods.add(Period(1, startTime, endTime, homeTeacherSubject, homeTeacherId, homeTeacherName))
                    } else if (p1.teacherId.isEmpty()) {
                        periods[periods.indexOf(p1)] = p1.copy(
                            subject = homeTeacherSubject, 
                            teacherId = homeTeacherId, 
                            teacherName = homeTeacherName
                        )
                    }
                    initialMap[day] = daySched.copy(periods = periods.sortedBy { it.periodNumber })
                }
            }
        }
        mutableStateOf(initialMap)
    }

    // Dynamic Quota Calculation
    val remainingQuotas = remember(localSchedule, quotas) {
        val usedCounts = mutableMapOf<String, Int>()
        localSchedule.values.forEach { daySched ->
            daySched.periods.forEach { period ->
                if (period.subject.isNotEmpty()) {
                    usedCounts[period.subject] = usedCounts.getOrDefault(period.subject, 0) + 1
                }
            }
        }
        quotas.mapValues { (subject, quota) ->
            quota.copy(classCount = quota.classCount - usedCounts.getOrDefault(subject, 0))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.edit_timetable), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveWeeklySchedule(className, localSchedule)
                            Toast.makeText(context, "Timetable saved!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Big Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Time Table For $className",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Horizontal Quota Cards (Q)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(remainingQuotas.values.toList()) { quota ->
                            QCard(quota, quotas[quota.subject]?.classCount ?: 0)
                        }
                    }
                }
            }

            // Scrollable Table Entity
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column {
                    // Days Row
                    Row(
                        modifier = Modifier.padding(start = 100.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        days.forEach { day ->
                            DayLabelCard(day.take(3))
                        }
                    }

                    // Periods and Subject Grid
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(periodCount) { pIndex ->
                            val periodNumber = pIndex + 1
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Period Card (P)
                                PCard(periodNumber)

                                // Subject Cards (S) for each day
                                days.forEach { day ->
                                    val currentPeriod = localSchedule[day]?.periods?.find { it.periodNumber == periodNumber }
                                    SCard(
                                        period = currentPeriod,
                                        availableQuotas = remainingQuotas.values.toList(),
                                        day = day,
                                        periodNumber = periodNumber,
                                        currentClassName = className,
                                        viewModel = viewModel,
                                        onSubjectSelected = { selectedQuota ->
                                            val newSchedule = localSchedule.toMutableMap()
                                            val daySched = newSchedule[day] ?: DaySchedule()
                                            val periods = daySched.periods.toMutableList()
                                            val existing = periods.find { it.periodNumber == periodNumber }
                                            val newP = Period(
                                                periodNumber, 
                                                getStartTime(periodNumber), 
                                                getEndTime(periodNumber),
                                                selectedQuota.subject,
                                                selectedQuota.teacherId,
                                                selectedQuota.teacherName
                                            )
                                            if (existing != null) {
                                                periods[periods.indexOf(existing)] = newP
                                            } else {
                                                periods.add(newP)
                                            }
                                            newSchedule[day] = daySched.copy(periods = periods.sortedBy { it.periodNumber })
                                            localSchedule = newSchedule
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QCard(remainingQuota: SubjectQuota, totalQuota: Int) {
    val color = getSubjectColorForGrid(remainingQuota.subject)
    Surface(
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${remainingQuota.subject} ${remainingQuota.classCount}/$totalQuota",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color
            )
        }
    }
}

@Composable
fun DayLabelCard(day: String) {
    Surface(
        modifier = Modifier.size(width = 120.dp, height = 40.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(day, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun PCard(periodNumber: Int) {
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
                text = stringResource(R.string.period_label, periodNumber).split(" ").first() + " " + periodNumber, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 16.sp, 
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${getStartTime(periodNumber)}\n${getEndTime(periodNumber)}", 
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun SCard(
    period: Period?,
    availableQuotas: List<SubjectQuota>,
    day: String,
    periodNumber: Int,
    currentClassName: String,
    viewModel: TimetableViewModel,
    onSubjectSelected: (SubjectQuota) -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val color = if (period != null) getSubjectColorForGrid(period.subject) else Color.DarkGray

    Card(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clickable { showMenu = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (period != null) color.copy(alpha = 0.1f) else color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, if (period != null) color.copy(alpha = 0.5f) else color.copy(alpha = 0.2f))
    ) {
        if (period != null) {
            Box(modifier = Modifier.padding(10.dp).fillMaxSize()) {
                Text(
                    text = period.subject,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = color,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                Text(
                    text = period.teacherName,
                    fontSize = 10.sp,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomEnd),
                    maxLines = 1
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.leisure_period), 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 12.sp, 
                    color = color.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text(stringResource(R.string.select_subject_dialog_title)) },
            text = {
                val filterQuotas = availableQuotas.filter { it.classCount > 0 }
                if (filterQuotas.isEmpty()) {
                    Text(stringResource(R.string.no_quota_left))
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterQuotas) { quota ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clickable { 
                                        val conflictClass = viewModel.getTeacherConflict(
                                            quota.teacherId, day, periodNumber, currentClassName
                                        )
                                        if (conflictClass == null) {
                                            onSubjectSelected(quota)
                                            showMenu = false 
                                        } else {
                                            Toast.makeText(
                                                context, 
                                                context.getString(R.string.teacher_conflict_error, quota.teacherName, conflictClass), 
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = getSubjectColorForGrid(quota.subject).copy(alpha = 0.1f)),
                                border = BorderStroke(1.dp, getSubjectColorForGrid(quota.subject).copy(alpha = 0.4f))
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(quota.subject, fontWeight = FontWeight.Bold, color = getSubjectColorForGrid(quota.subject))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenu = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

fun getStartTime(periodNumber: Int): String {
    return when (periodNumber) {
        1 -> "09:00 AM"
        2 -> "09:45 AM"
        3 -> "10:30 AM"
        4 -> "11:30 AM"
        5 -> "12:15 PM"
        6 -> "02:00 PM"
        7 -> "02:45 PM"
        8 -> "03:45 PM"
        9 -> "04:30 PM"
        10 -> "05:15 PM"
        else -> ""
    }
}

fun getEndTime(periodNumber: Int): String {
    return when (periodNumber) {
        1 -> "09:45 AM"
        2 -> "10:30 AM"
        3 -> "11:15 AM"
        4 -> "12:15 PM"
        5 -> "01:00 PM"
        6 -> "02:45 PM"
        7 -> "03:30 PM"
        8 -> "04:30 PM"
        9 -> "05:15 PM"
        10 -> "05:45 PM"
        else -> ""
    }
}
