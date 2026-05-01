package com.project.markmyday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.ui.theme.MarkMyDayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(
    onBack: () -> Unit,
    onApply: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply Leave", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LeaveScreenContent(
            onApply = onApply,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun LeaveScreenContent(
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    var leaveType by remember { mutableStateOf("Sick Leave") }
    
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Select Leave Type", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Sick Leave", "Casual", "Medical").forEach { type ->
                LeaveChip(text = type, selected = leaveType, onClick = { leaveType = it })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Select Dates", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        CalendarPlaceholder()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FakeDateBox("Starts: 15 Oct 2023")
        Spacer(modifier = Modifier.height(8.dp))
        FakeDateBox("Ends: 16 Oct 2023")

        Spacer(modifier = Modifier.height(24.dp))
        Text("Reason for Leave", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().height(120.dp),
            placeholder = { Text("Enter your reason here...") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Submit Application", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LeaveChip(text: String, selected: String, onClick: (String) -> Unit) {
    val isSelected = text == selected

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
        modifier = Modifier
            .padding(4.dp)
            .height(36.dp)
            .clickable { onClick(text) }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun FakeDateBox(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text)
        }
    }
}

@Composable
fun CalendarPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Calendar UI Placeholder", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun LeaveScreenPreview() {
    MarkMyDayTheme {
        LeaveScreen(onBack = {}, onApply = {})
    }
}


@Composable
fun LeaveApproveScareen(){

}