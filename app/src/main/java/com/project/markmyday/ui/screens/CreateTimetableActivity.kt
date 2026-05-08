package com.project.markmyday.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.LocalSettingsViewModel
import com.project.markmyday.R
import com.project.markmyday.data.model.Teacher
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.SettingsViewModel
import com.project.markmyday.viewmodel.TimetableState
import com.project.markmyday.viewmodel.TimetableViewModel

class CreateTimetableActivity : AppCompatActivity() {
    
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val timetableViewModel: TimetableViewModel by viewModels()

            CompositionLocalProvider(LocalSettingsViewModel provides settingsViewModel) {
                MarkMyDayTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CreateTimetableScreen(
                            onBack = { finish() },
                            viewModel = timetableViewModel
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTimetableScreen(
    onBack: () -> Unit,
    viewModel: TimetableViewModel = viewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val state by viewModel.state.collectAsState()
    val assignments by viewModel.classTeacherAssignments.collectAsState()
    val teachers by viewModel.allTeachers.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.create_timetable_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state is TimetableState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.loading_step),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Button(
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = state !is TimetableState.Loading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (currentStep == 3) stringResource(R.string.submit) else stringResource(R.string.next),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TimetableStepper(
                currentStep = currentStep,
                onStepClick = { step ->
                    if (step < currentStep) {
                        viewModel.goToStep(step)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "StepTransition"
                ) { step ->
                    when (step) {
                        1 -> Step1Content(teachers, assignments, onAssign = { cls, t -> viewModel.assignTeacherToClass(cls, t) })
                        2 -> StepPlaceholder(stringResource(R.string.step_2_name))
                        3 -> StepPlaceholder(stringResource(R.string.step_3_name))
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableStepper(
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val step = index + 1
                val isActive = step <= currentStep
                val isClickable = step < currentStep

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .then(
                            if (isClickable) Modifier.clickable { onStepClick(step) } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.toString(),
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                if (index < 2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                if (step < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        val stepName = when (currentStep) {
            1 -> stringResource(R.string.step_1_name)
            2 -> stringResource(R.string.step_2_name)
            3 -> stringResource(R.string.step_3_name)
            else -> ""
        }

        Text(
            text = stepName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun Step1Content(
    allTeachers: List<Teacher>,
    assignments: Map<String, Teacher?>,
    onAssign: (String, Teacher?) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(assignments.keys.toList()) { className ->
            ClassTeacherCard(
                className = className,
                allTeachers = allTeachers,
                selectedTeacher = assignments[className],
                onTeacherSelected = { onAssign(className, it) }
            )
        }
    }
}

@Composable
fun ClassTeacherCard(
    className: String,
    allTeachers: List<Teacher>,
    selectedTeacher: Teacher?,
    onTeacherSelected: (Teacher?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Filtering logic based on rules
    val classNum = className.split(" ").last().toIntOrNull() ?: 0
    val filteredTeachers = allTeachers.filter { teacher ->
        when {
            classNum in 1..5 -> teacher.classesTaughtCategories.contains("Primary")
            classNum in 6..7 -> teacher.classesTaughtCategories.contains("Secondary")
            classNum in 8..10 -> teacher.classesTaughtCategories.contains("High School")
            else -> false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = className,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = if (selectedTeacher == null) stringResource(R.string.select_teacher) else selectedTeacher.name,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (selectedTeacher != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TeacherInfoCompact(teacher = selectedTeacher)
            }
        }
    }

    if (expanded) {
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text(stringResource(R.string.select_teacher)) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(filteredTeachers) { teacher ->
                        TeacherSelectionItem(
                            teacher = teacher,
                            isSelected = selectedTeacher?.teacherId == teacher.teacherId,
                            onSelect = {
                                onTeacherSelected(teacher)
                                expanded = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { expanded = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun TeacherInfoCompact(teacher: Teacher) {
    val subjectColor = getSubjectColor(teacher.subject)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = subjectColor.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, subjectColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle with Initial
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(subjectColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teacher.name.firstOrNull()?.toString()?.uppercase() ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = subjectColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teacher.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Subject Pill
                    SubjectBadge(subject = teacher.subject, color = subjectColor)
                    
                    // Category Pills
                    teacher.classesTaughtCategories.forEach { category ->
                        CategoryBadge(category = category)
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectBadge(subject: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = subject,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun CategoryBadge(category: String) {
    val (containerColor, textColor) = when (category.lowercase()) {
        "primary" -> Color(0xFF4CAF50).copy(alpha = 0.15f) to Color(0xFF2E7D32)
        "secondary" -> Color(0xFF2196F3).copy(alpha = 0.15f) to Color(0xFF1565C0)
        "high school" -> Color(0xFFFF9800).copy(alpha = 0.15f) to Color(0xFFEF6C00)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) to MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun TeacherSelectionItem(
    teacher: Teacher,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val subjectColor = getSubjectColor(teacher.subject)
    
    Surface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) subjectColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) subjectColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(subjectColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teacher.name.firstOrNull()?.toString()?.uppercase() ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = subjectColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teacher.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SubjectBadge(subject = teacher.subject, color = subjectColor)
                    teacher.classesTaughtCategories.forEach { category ->
                        CategoryBadge(category = category)
                    }
                }
            }
        }
    }
}

@Composable
fun getSubjectColor(subject: String): Color {
    return when (subject.lowercase()) {
        "telugu" -> Color(0xFF1E88E5) // Blue
        "math" -> Color(0xFF9E9E9E) // Grey
        "physics" -> Color(0xFF03A9F4) // Light Blue
        "biology" -> Color(0xFF4CAF50) // Green
        "science" -> Color(0xFF26A69A) // Mix of Blue and Green (Teal)
        "social" -> Color(0xFFFF9800) // Orange
        "japanese" -> Color(0xFF212121) // Black
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
fun StepPlaceholder(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Placeholder for $name", style = MaterialTheme.typography.headlineSmall)
    }
}
