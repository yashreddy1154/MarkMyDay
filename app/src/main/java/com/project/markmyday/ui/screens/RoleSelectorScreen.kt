package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoleSelectorScreen(onRoleSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select Dashboard", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))
        
        Button(
            onClick = { onRoleSelected("student") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Student Dashboard")
        }
        
        Button(
            onClick = { onRoleSelected("teacher") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Teacher Dashboard")
        }
        
        Button(
            onClick = { onRoleSelected("admin") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Admin Dashboard")
        }
    }
}
