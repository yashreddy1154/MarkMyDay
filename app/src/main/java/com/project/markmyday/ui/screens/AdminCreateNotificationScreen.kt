package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.data.model.NotificationData
import com.project.markmyday.viewmodel.AdminNotificationViewModel
import com.project.markmyday.viewmodel.NotificationUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val notifications by viewModel.notifications.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val audiences = listOf("all", "teachers")
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

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

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Notification") },
            text = { Text("Are you sure you want to delete this notice? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm?.let { viewModel.deleteNotification(it) }
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Notice Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Create New Notice",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                    // Button Fix (Loading indicator + Disabled state)
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

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Notice History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }

            if (notifications.isEmpty()) {
                item {
                    Text(
                        "No past notices found.",
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(notifications, key = { it.id }) { notification ->
                    NotificationHistoryItem(
                        notification = notification,
                        onDelete = { showDeleteConfirm = notification.id }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationHistoryItem(
    notification: NotificationData,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateString = remember(notification.timestamp) { sdf.format(Date(notification.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.heading,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "To: ${notification.audience.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Notice",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By: ${notification.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
