package com.project.markmyday.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Person
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
import com.project.markmyday.data.model.Period
import com.project.markmyday.data.model.Timetable
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.TimetableViewModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun getSubjectColorForStudent(subject: String): Color {
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

class StudentTimetableActivity : AppCompatActivity() {

    private val viewModel: TimetableViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val className = intent.getStringExtra("className") ?: ""

        setContent {
            MarkMyDayTheme {
                StudentTimetableContent(
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
fun StudentTimetableContent(
    className: String,
    viewModel: TimetableViewModel,
    onBack: () -> Unit
) {
    val timetables by viewModel.allTimetables.collectAsState()
    val currentTimetable = timetables.find { it.className == className }
    
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val pagerState = rememberPagerState(pageCount = { days.size })
    val scope = rememberCoroutineScope()

    // Determine current day to set initial page
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
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Day Tabs
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
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
                val daySchedule = currentTimetable?.weeklySchedule?.get(day)
                val periods = daySchedule?.periods?.filter { it.subject.isNotEmpty() } ?: emptyList()

                if (periods.isEmpty()) {
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
                        items(periods) { period ->
                            StudentPeriodCard(period)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentPeriodCard(period: Period) {
    val subjectColor = getSubjectColorForStudent(period.subject)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, subjectColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = period.subject,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = subjectColor
                )
                
                Surface(
                    color = subjectColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "P${period.periodNumber}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = subjectColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = null, 
                    tint = subjectColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = period.teacherName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime, 
                    contentDescription = null, 
                    tint = subjectColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${period.startTime} - ${period.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = subjectColor
                )
            }
        }
    }
}
