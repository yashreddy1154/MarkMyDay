package com.project.markmyday.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.R
import com.project.markmyday.data.model.Period
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.DiaryState
import com.project.markmyday.viewmodel.DigitalDiaryViewModel
import com.project.markmyday.viewmodel.TimetableViewModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun getSubjectColorForTeacher(subject: String): Color {
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

class TeacherTimetableActivity : AppCompatActivity() {

    private val viewModel: TimetableViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val teacherName = intent.getStringExtra("teacherName") ?: ""

        setContent {
            MarkMyDayTheme {
                TeacherTimetableContent(
                    teacherName = teacherName,
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTimetableContent(
    teacherName: String,
    viewModel: TimetableViewModel,
    onBack: () -> Unit,
    diaryViewModel: DigitalDiaryViewModel = viewModel()
) {
    val timetables by viewModel.allTimetables.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val diaryState by diaryViewModel.state.collectAsState()
    val context = LocalContext.current
    
    val teacherId = remember(allTeachers, teacherName) {
        allTeachers.find { it.name.equals(teacherName, ignoreCase = true) }?.teacherId ?: ""
    }

    var showDiaryDialog by remember { mutableStateOf<Pair<String, Period>?>(null) }

    LaunchedEffect(diaryState) {
        if (diaryState is DiaryState.Success) {
            android.widget.Toast.makeText(context, R.string.diary_post_success, android.widget.Toast.LENGTH_SHORT).show()
            diaryViewModel.resetState()
            showDiaryDialog = null
        } else if (diaryState is DiaryState.Error) {
            android.widget.Toast.makeText(context, (diaryState as DiaryState.Error).message, android.widget.Toast.LENGTH_SHORT).show()
            diaryViewModel.resetState()
        }
    }

    if (showDiaryDialog != null) {
        val (className, period) = showDiaryDialog!!
        PostDiaryDialog(
            className = className,
            subject = period.subject,
            onDismiss = { showDiaryDialog = null },
            onPost = { note, homework ->
                diaryViewModel.postEntry(
                    className = className,
                    subject = period.subject,
                    teacherId = teacherId,
                    teacherName = teacherName,
                    note = note,
                    homework = homework
                )
            },
            isLoading = diaryState is DiaryState.Loading
        )
    }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val pagerState = rememberPagerState(pageCount = { days.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val initialIndex = when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            else -> 0
        }
        pagerState.scrollToPage(initialIndex)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.tile_timetables), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                divider = {}
            ) {
                days.forEachIndexed { index, day ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(day.take(3), fontWeight = FontWeight.Bold) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val day = days[page]
                val teacherPeriods = remember(timetables, teacherId, day) {
                    val entries = mutableListOf<Pair<String, Period>>()
                    timetables.forEach { timetable ->
                        val daySched = timetable.weeklySchedule[day]
                        daySched?.periods?.filter { it.teacherId == teacherId }?.forEach { period ->
                            entries.add(timetable.className to period)
                        }
                    }
                    entries.sortedBy { it.second.periodNumber }
                }

                if (teacherPeriods.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.leisure_period),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(teacherPeriods) { (className, period) ->
                            TeacherPeriodCard(className, period, onPostDiary = {
                                showDiaryDialog = className to period
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherPeriodCard(className: String, period: Period, onPostDiary: () -> Unit) {
    val sectionColor = com.project.markmyday.ui.utils.TeacherUtils.getClassColor(className)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = sectionColor.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, sectionColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = className,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = sectionColor
                    )
                    Text(
                        text = period.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = sectionColor.copy(alpha = 0.8f)
                    )
                }
                
                Surface(
                    color = sectionColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "P${period.periodNumber}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = sectionColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = sectionColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${period.startTime} - ${period.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = sectionColor
                    )
                }
                
                Surface(
                    onClick = onPostDiary,
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, sectionColor.copy(alpha = 0.6f)),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = "Post Diary",
                            tint = sectionColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostDiaryDialog(
    className: String,
    subject: String,
    onDismiss: () -> Unit,
    onPost: (String, String) -> Unit,
    isLoading: Boolean
) {
    var note by remember { mutableStateOf("") }
    var homework by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.digital_diary) + " - $subject ($className)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.what_was_taught)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = homework,
                    onValueChange = { homework = it },
                    label = { Text(stringResource(R.string.homework_for_tomorrow)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onPost(note, homework) },
                enabled = !isLoading && (note.isNotBlank() || homework.isNotBlank())
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.post_to_diary))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
