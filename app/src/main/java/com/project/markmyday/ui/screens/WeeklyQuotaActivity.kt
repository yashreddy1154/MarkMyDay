package com.project.markmyday.ui.screens

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.R
import com.project.markmyday.data.model.SubjectQuota
import com.project.markmyday.data.model.Teacher
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.TimetableViewModel

@Composable
fun getSubjectColorInternal(subject: String): Color {
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

class WeeklyQuotaActivity : AppCompatActivity() {
    
    private val viewModel: TimetableViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val className = intent.getStringExtra("className") ?: ""
        
        setContent {
            MarkMyDayTheme {
                WeeklyQuotaContent(
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
fun WeeklyQuotaContent(
    className: String, 
    viewModel: TimetableViewModel,
    onBack: () -> Unit
) {
    val timetables by viewModel.allTimetables.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    
    val classNum = className.split(" ").last().toIntOrNull() ?: 0
    val maxClasses = if (classNum in 1..5) 42 else 60
    
    val subjects = if (classNum in 8..10) {
        listOf(
            stringResource(R.string.subject_telugu_label),
            stringResource(R.string.subject_hindi_label),
            stringResource(R.string.subject_english_label),
            stringResource(R.string.subject_math_label),
            stringResource(R.string.subject_physics_label),
            stringResource(R.string.subject_biology_label),
            stringResource(R.string.subject_social_label)
        )
    } else {
        listOf(
            stringResource(R.string.subject_telugu_label),
            stringResource(R.string.subject_hindi_label),
            stringResource(R.string.subject_english_label),
            stringResource(R.string.subject_math_label),
            stringResource(R.string.subject_science_label),
            stringResource(R.string.subject_social_label)
        )
    }

    // Initialize local state from existing data or defaults
    val existingTimetable = timetables.find { it.className == className }
    val homeTeacherId = existingTimetable?.homeTeacherId ?: ""

    val category = when {
        classNum in 1..5 -> "Primary"
        classNum in 6..7 -> "Secondary"
        else -> "High School"
    }
    val homeTeacher = allTeachers.find { it.teacherId == homeTeacherId }

    var localQuotas by remember(existingTimetable) {
        val initialMap = if (existingTimetable?.weeklyQuota.isNullOrEmpty()) {
            viewModel.getPreFilledQuota(category, homeTeacher).toMutableMap()
        } else {
            subjects.associateWith { subject ->
                existingTimetable?.weeklyQuota?.get(subject) ?: SubjectQuota(subject = subject)
            }.toMutableMap()
        }
        mutableStateOf(initialMap)
    }

    val currentTotal = localQuotas.values.sumOf { it.classCount }

    // Auto-select Home Teacher logic
    LaunchedEffect(allTeachers, homeTeacherId) {
        if (homeTeacherId.isNotEmpty()) {
            val homeTeacher = allTeachers.find { it.teacherId == homeTeacherId }
            if (homeTeacher != null) {
                val subject = homeTeacher.subject
                val currentQuota = localQuotas[subject]
                if (currentQuota != null && currentQuota.teacherId != homeTeacherId) {
                    val newMap = localQuotas.toMutableMap()
                    newMap[subject] = currentQuota.copy(
                        teacherId = homeTeacher.teacherId,
                        teacherName = homeTeacher.name
                    )
                    localQuotas = newMap
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.weekly_quota_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            val localContext = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    if (currentTotal <= maxClasses) {
                        // 1. Save the finalized quota
                        viewModel.saveWeeklyQuota(className, localQuotas, currentTotal)

                        // 🪄 AI STEP 2: Generate the Timetable Grid in the background!
                        if (homeTeacherId.isNotEmpty()) {
                            viewModel.generateAndSaveScheduleFromQuota(className, category, localQuotas, homeTeacherId)
                        }

                        onBack()
                    } else {
                        Toast.makeText(localContext, localContext.getString(R.string.quota_limit_error), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = className,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = stringResource(R.string.total_classes_format, currentTotal, maxClasses),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (currentTotal > maxClasses) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(subjects) { subject ->
                    val quota = localQuotas[subject] ?: SubjectQuota(subject = subject)
                    SubjectQuotaCard(
                        subject = subject,
                        quota = quota,
                        allTeachers = allTeachers,
                        classNum = classNum,
                        homeTeacherId = homeTeacherId,
                        onQuotaChange = { updatedQuota ->
                            val newMap = localQuotas.toMutableMap()
                            newMap[subject] = updatedQuota
                            localQuotas = newMap
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectQuotaCard(
    subject: String,
    quota: SubjectQuota,
    allTeachers: List<Teacher>,
    classNum: Int,
    homeTeacherId: String,
    onQuotaChange: (SubjectQuota) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showTeacherDialog by remember { mutableStateOf(false) }
    
    // Filter teachers by subject and category
    val filteredTeachers = allTeachers.filter { teacher ->
        val categoryMatch = when {
            classNum in 1..5 -> teacher.classesTaughtCategories.contains("Primary")
            classNum in 6..7 -> teacher.classesTaughtCategories.contains("Secondary")
            classNum in 8..10 -> teacher.classesTaughtCategories.contains("High School")
            else -> false
        }
        teacher.subject.equals(subject, ignoreCase = true) && categoryMatch
    }

    val hasHomeTeacherInList = filteredTeachers.any { it.teacherId == homeTeacherId }

    val isLanguage = subject.lowercase() in listOf(
        stringResource(R.string.subject_telugu_label).lowercase(),
        stringResource(R.string.subject_hindi_label).lowercase(),
        stringResource(R.string.subject_english_label).lowercase()
    )
    val subjectColor = getSubjectColorInternal(subject)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, subjectColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = subjectColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    onClick = { showTeacherDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = subjectColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, subjectColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = if (quota.teacherName.isEmpty()) stringResource(R.string.select_teacher) else quota.teacherName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = subjectColor
                    )
                }

                if (isLanguage) {
                    Text(
                        text = stringResource(R.string.max_periods_hint, 7),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            // Counter Graphic
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { if (quota.classCount > 0) onQuotaChange(quota.copy(classCount = quota.classCount - 1)) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(subjectColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = subjectColor)
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(subjectColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, subjectColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = quota.classCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = subjectColor
                    )
                }

                IconButton(
                    onClick = { 
                        if (!isLanguage || quota.classCount < 7) {
                            onQuotaChange(quota.copy(classCount = quota.classCount + 1))
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isLanguage && quota.classCount >= 7) Color.Gray.copy(alpha = 0.3f) else subjectColor, 
                            RoundedCornerShape(8.dp)
                        ),
                    enabled = !isLanguage || quota.classCount < 7
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                }
            }
        }
    }

    if (showTeacherDialog) {
        AlertDialog(
            onDismissRequest = { showTeacherDialog = false },
            title = { Text(stringResource(R.string.select_teacher_for_subject, subject)) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    if (filteredTeachers.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.no_teachers_found_error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(filteredTeachers) { teacher ->
                            val isHomeTeacher = teacher.teacherId == homeTeacherId
                            val isClickable = !hasHomeTeacherInList || isHomeTeacher
                            
                            TeacherSelectionItem(
                                teacher = teacher,
                                isSelected = quota.teacherId == teacher.teacherId,
                                onSelect = {
                                    if (isClickable) {
                                        onQuotaChange(quota.copy(teacherId = teacher.teacherId, teacherName = teacher.name))
                                        showTeacherDialog = false
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.home_teacher_error), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                isHomeTeacher = isHomeTeacher
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTeacherDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
