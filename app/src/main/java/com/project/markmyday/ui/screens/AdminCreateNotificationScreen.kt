package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.viewmodel.AdminNotificationViewModel
import com.project.markmyday.viewmodel.NotificationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateNotificationScreen(
    onBack: () -> Unit
) {
    val viewModel: AdminNotificationViewModel = viewModel()
    val heading by viewModel.heading.collectAsState()
    val message by viewModel.message.collectAsState()
    val author by viewModel.author.collectAsState()
    val targetAudience by viewModel.targetAudience.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val audiences = listOf("all", "teachers")

    // Handle UI state changes for snackbar
    LaunchedEffect(uiState) {
        when (uiState) {
            is NotificationUiState.Success -> {
                snackbarHostState.showSnackbar("Notification sent successfully!")
                viewModel.resetUiState()
            }
            is NotificationUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as NotificationUiState.Error).message)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Notification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Send a message to your community",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Heading Field
            OutlinedTextField(
                value = heading,
                onValueChange = { viewModel.onHeadingChange(it) },
                label = { Text("Notification Heading") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter a catchy title...") }
            )

            // Author Field
            OutlinedTextField(
                value = author,
                onValueChange = { viewModel.onAuthorChange(it) },
                label = { Text("Author / Department") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g. Administration, Principal...") }
            )

            // Message Field
            OutlinedTextField(
                value = message,
                onValueChange = { viewModel.onMessageChange(it) },
                label = { Text("Notification Message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 10,
                placeholder = { Text("Enter the details of the notification...") }
            )

            Text(
                text = "Target Audience",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Column(Modifier.selectableGroup()) {
                audiences.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (text == targetAudience),
                                onClick = { viewModel.onTargetAudienceChange(text) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == targetAudience),
                            onClick = null
                        )
                        Text(
                            text = text.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Button Fix (Loading indicator + Disabled state)
            Button(
                onClick = { viewModel.sendNotification() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is NotificationUiState.Loading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is NotificationUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Notification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
