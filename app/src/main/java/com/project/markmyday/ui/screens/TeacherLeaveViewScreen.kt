package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.data.model.LeaveRequest
import com.project.markmyday.viewmodel.LeaveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLeaveViewScreen(
    onBack: () -> Unit,
    viewModel: LeaveViewModel = viewModel()
) {
    val allLeaves by viewModel.allLeaves.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Leaves", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (allLeaves.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No leave records found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allLeaves, key = { it.id }) { request ->
                    TeacherLeaveCard(request = request)
                }
            }
        }
    }
}

@Composable
fun TeacherLeaveCard(request: LeaveRequest) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    
    val (statusIcon, statusColor) = when (request.status) {
        "approved" -> Icons.Default.Check to Color(0xFF4CAF50)
        "rejected" -> Icons.Default.Close to MaterialTheme.colorScheme.error
        else -> Icons.Default.History to Color(0xFFFFA000)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(request.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val start = request.startDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
                val end = request.endDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
                Text("$start - $end • Class: ${request.classSection}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(request.reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Icon(
                    statusIcon,
                    contentDescription = request.status,
                    modifier = Modifier.padding(6.dp),
                    tint = statusColor
                )
            }
        }
    }
}
