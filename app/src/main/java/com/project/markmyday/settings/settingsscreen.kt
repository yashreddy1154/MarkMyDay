package com.project.markmyday.settings


// Update this to match your actual package name

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.viewmodel.SettingsUiState
import com.project.markmyday.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle logout event from ViewModel
    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MarkMyDay Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profile Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    when (val state = uiState) {
                        is SettingsUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        is SettingsUiState.Success -> {
                            Text("Name: ${state.profile.name}")
                            Text("Role: ${state.profile.role}")
                        }
                        is SettingsUiState.Error -> {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // 2. Language Toggle
            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val languages = listOf(
                "English" to "en",
                "Hindi" to "hi",
                "Telugu" to "te"
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Language", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        languages.forEach { (name, code) ->
                            FilterChip(
                                selected = currentLanguage.contains(code),
                                onClick = {
                                    viewModel.setLanguage(code)
                                },
                                label = { Text(name) }
                            )
                        }
                    }
                }
            }

            // 3. About Developers
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About Developers", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Core Team: Tarak & Yash")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Logout Button
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }
}
