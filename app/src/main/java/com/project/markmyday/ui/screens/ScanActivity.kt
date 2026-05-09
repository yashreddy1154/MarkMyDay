package com.project.markmyday.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.project.markmyday.ui.theme.MarkMyDayTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class ScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkMyDayTheme {
                ScanScreen(
                    onBack = { finish() },
                    onScanSuccess = { navigateToStatus(true, it) },
                    onScanFailure = { navigateToStatus(false, it) }
                )
            }
        }
    }

    private fun navigateToStatus(success: Boolean, message: String) {
        val intent = Intent(this, AttendanceStatusActivity::class.java).apply {
            putExtra("IS_SUCCESS", success)
            putExtra("MESSAGE", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onScanSuccess: (String) -> Unit,
    onScanFailure: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CameraPreview(
                    onBarcodeDetected = { barcodeValue ->
                        if (barcodeValue == "MARK_MY_DAY_GATE_01") {
                            handleSuccessfulScan(onScanSuccess, onScanFailure)
                        } else {
                            Log.d("ScanActivity", "Invalid QR code: $barcodeValue")
                        }
                    }
                )
                
                // Overlay for scanner target
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Visual guide could be added here
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera permission is required to scan QR codes.")
            }
        }
    }
}

@Composable
fun CameraPreview(
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    
    // Flag to prevent multiple scans
    var isScanning by remember { mutableStateOf(true) }

    val barcodeScannerOptions = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }
    val barcodeScanner = remember { BarcodeScanning.getClient(barcodeScannerOptions) }

    LaunchedEffect(cameraController) {
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(context)
            ) { result ->
                if (!isScanning) return@MlKitAnalyzer
                
                val barcodes = result.getValue(barcodeScanner)
                if (!barcodes.isNullOrEmpty()) {
                    val barcodeValue = barcodes[0].rawValue
                    if (barcodeValue != null) {
                        isScanning = false
                        onBarcodeDetected(barcodeValue)
                    }
                }
            }
        )
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                controller = cameraController
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun handleSuccessfulScan(
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    if (uid == null) {
        onFailure("User not authenticated")
        return
    }

    // First, find the teacherId from the users collection
    firestore.collection("users").document(uid).get()
        .addOnSuccessListener { userDoc ->
            val teacherId = userDoc.getString("teacherId")
            if (teacherId == null) {
                onFailure("Teacher ID not found for user")
                return@addOnSuccessListener
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val attendanceData = mapOf(
                "status" to "Present",
                "time" to FieldValue.serverTimestamp(),
                "method" to "QR_Scan"
            )

            firestore.collection("teachers")
                .document(teacherId)
                .collection("attendance_logs")
                .document(today)
                .set(attendanceData)
                .addOnSuccessListener {
                    onSuccess("Attendance marked for $today")
                }
                .addOnFailureListener { e ->
                    onFailure("Failed to update Firestore: ${e.localizedMessage}")
                }
        }
        .addOnFailureListener { e ->
            onFailure("Failed to fetch user data: ${e.localizedMessage}")
        }
}
