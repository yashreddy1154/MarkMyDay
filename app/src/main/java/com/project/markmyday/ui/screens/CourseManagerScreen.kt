package com.project.markmyday.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.data.model.CourseVideo
import com.project.markmyday.utils.FileParser
import com.project.markmyday.viewmodel.CourseUploadStatus
import com.project.markmyday.viewmodel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseManagerScreen(
    onBack: () -> Unit
) {
    val viewModel: CourseViewModel = viewModel()
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var subject by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }

    val classes = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
    var classExpanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadCsv(context, it) }
    }

    LaunchedEffect(uploadStatus) {
        when (uploadStatus) {
            is CourseUploadStatus.Loading -> {
                // Toast.makeText(context, "Processing...", Toast.LENGTH_SHORT).show()
            }
            is CourseUploadStatus.Success -> {
                val count = (uploadStatus as CourseUploadStatus.Success).count
                Toast.makeText(context, "Success! $count course(s) updated.", Toast.LENGTH_LONG).show()
                // Reset fields on manual success
                title = ""; youtubeUrl = ""
                viewModel.resetStatus()
            }
            is CourseUploadStatus.Error -> {
                val error = (uploadStatus as CourseUploadStatus.Error).message
                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                viewModel.resetStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Manager", fontWeight = FontWeight.Bold) },
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
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Bulk CSV Section
            Text("Bulk Upload", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable { launcher.launch("*/*") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Upload CSV/Excel File", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            // 2. Manual Entry Section
            Text("Add Lesson Manually", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Class Selection
                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = !classExpanded }
                ) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Class") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = { classExpanded = false }
                    ) {
                        classes.forEach { classNum ->
                            DropdownMenuItem(
                                text = { Text("Class $classNum") },
                                onClick = {
                                    className = "Class $classNum"
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. Mathematics)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.VideoLibrary, contentDescription = null) }
                )

                // Video Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Lesson Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // YouTube Link
                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("Paste YouTube Link") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    placeholder = { Text("https://www.youtube.com/watch?v=...") }
                )

                if (uploadStatus is CourseUploadStatus.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Update Button
                Button(
                    onClick = {
                        if (className.isNotEmpty() && subject.isNotEmpty() && title.isNotEmpty() && youtubeUrl.isNotEmpty()) {
                            val videoId = FileParser.extractYoutubeId(youtubeUrl)
                            if (videoId != null) {
                                viewModel.saveManualCourse(
                                    CourseVideo(
                                        video_id = videoId,
                                        subject = subject,
                                        class_level = className,
                                        title = title
                                    )
                                )
                            } else {
                                Toast.makeText(context, "Error: Invalid YouTube Link", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uploadStatus !is CourseUploadStatus.Loading
                ) {
                    Text("Update Course for Students", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
