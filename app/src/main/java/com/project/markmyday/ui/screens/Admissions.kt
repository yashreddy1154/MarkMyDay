package com.project.markmyday.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.R
import com.project.markmyday.data.model.Admission
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.AdmissionsViewModel
import com.project.markmyday.viewmodel.AuthResult
import com.project.markmyday.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmissionsScreen(
    role: String = "Staff",
    onBack: () -> Unit,
    admissionsViewModel: AdmissionsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val admissions by admissionsViewModel.admissions.collectAsState()
    val isLoading by admissionsViewModel.isLoading.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    var currentUserName = "Staff"
    var currentUserRole = role

    if (authState is AuthResult.Success) {
        val success = authState as AuthResult.Success
        currentUserName = success.name
        currentUserRole = success.role
    }

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var editingAdmission by remember { mutableStateOf<Admission?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Admission?>(null) }

    fun clearFields() {
        name = ""
        age = ""
        className = ""
        parentName = ""
        phone = ""
        editingAdmission = null
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_admission)) },
            text = { Text(stringResource(R.string.delete_admission_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { admissionsViewModel.deleteAdmission(it.id) }
                    showDeleteDialog = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.student_admissions), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (editingAdmission == null) stringResource(R.string.new_admission_form) else stringResource(R.string.edit_admission),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.student_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text(stringResource(R.string.age)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = className,
                                onValueChange = { className = it },
                                label = { Text(stringResource(R.string.class_label)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = parentName,
                            onValueChange = { parentName = it },
                            label = { Text(stringResource(R.string.parent_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(stringResource(R.string.phone_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (editingAdmission != null) {
                                OutlinedButton(
                                    onClick = { clearFields() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                            
                            Button(
                                onClick = {
                                    if (name.isNotBlank() && className.isNotBlank() && phone.isNotBlank()) {
                                        val admission = Admission(
                                            id = editingAdmission?.id ?: "",
                                            name = name,
                                            age = age,
                                            className = className,
                                            parentName = parentName,
                                            phone = phone,
                                            addedBy = currentUserRole,
                                            addedByName = currentUserName
                                        )

                                        if (editingAdmission == null) {
                                            admissionsViewModel.addAdmission(admission)
                                        } else {
                                            admissionsViewModel.updateAdmission(admission)
                                        }

                                        clearFields()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(if (editingAdmission == null) stringResource(R.string.submit_admission) else stringResource(R.string.update_admission))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "recent_admissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (isLoading && admissions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (admissions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                        Text(
                            stringResource(R.string.no_admissions_yet), 
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(admissions) { admission ->
                AdmissionCard(
                    admission = admission,
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${admission.phone}"))
                        context.startActivity(intent)
                    },
                    onMessage = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${admission.phone}"))
                        context.startActivity(intent)
                    },
                    onEdit = {
                        editingAdmission = admission
                        name = admission.name
                        age = admission.age
                        className = admission.className
                        parentName = admission.parentName
                        phone = admission.phone
                    },
                    onDelete = {
                        showDeleteDialog = admission
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AdmissionCard(
    admission: Admission,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = admission.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "Class: ${admission.className} • Age: ${admission.age}", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 14.sp
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Parent: ${admission.parentName}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = admission.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    
                    FilledIconButton(
                        onClick = onMessage,
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = "Message")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Added by: ${admission.addedByName} (${admission.addedBy})", 
                fontSize = 11.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdmissionsScreenPreview() {
    MarkMyDayTheme {
        AdmissionsScreen(role = "Admin", onBack = {})
    }
}
