package com.project.markmyday.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.markmyday.R
import com.project.markmyday.data.model.Period
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.TimetableViewModel
import kotlinx.coroutines.launch
import java.util.*

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
    onBack: () -> Unit
) {
    val timetables by viewModel.allTimetables.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    
    val teacherId = remember(allTeachers, teacherName) {
        allTeachers.find { it.name.equals(teacherName, ignoreCase = true) }?.teacherId ?: ""
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
                            TeacherPeriodCard(className, period)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherPeriodCard(className: String, period: Period) {
    val sectionColor = com.project.markmyday.ui.utils.TeacherUtils.getClassColor(className)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, sectionColor.copy(alpha = 0.4f))
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = period.subject,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    color = sectionColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "P${period.periodNumber}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = sectionColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                    color = sectionColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}
