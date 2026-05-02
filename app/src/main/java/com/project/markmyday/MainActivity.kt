package com.project.markmyday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import com.project.markmyday.ui.navigation.AppNavigation
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.project.markmyday.viewmodel.AuthViewModel
import com.project.markmyday.viewmodel.AuthResult
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment

import androidx.activity.result.contract.ActivityResultContracts

import com.project.markmyday.viewmodel.SettingsViewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

import androidx.appcompat.app.AppCompatActivity

val LocalSettingsViewModel = staticCompositionLocalOf<SettingsViewModel> {
    error("No SettingsViewModel provided")
}

class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        } else {
            Log.w("FCM", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for existing session
        authViewModel.checkSession()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val authState by authViewModel.authState.collectAsState()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            CompositionLocalProvider(LocalSettingsViewModel provides settingsViewModel) {
                MarkMyDayTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (authState) {
                            is AuthResult.Loading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is AuthResult.Success -> {
                                val result = authState as AuthResult.Success
                                val initialDashboardRoute = when (result.role.lowercase()) {
                                    "principal", "headmaster", "admin" -> "admin_dashboard/${result.name}/${result.role}"
                                    "teacher" -> "teacher_dashboard/${result.name}/${result.role}/${result.homeSection ?: "N/A"}/${result.subject ?: "N/A"}"
                                    "student" -> "student_dashboard/${result.name}/${result.role}"
                                    else -> "student_dashboard/${result.name}/${result.role}"
                                }
                                AppNavigation(
                                    startDestination = initialDashboardRoute,
                                    initialDashboardRoute = initialDashboardRoute
                                )
                            }
                            else -> {
                                AppNavigation()
                            }
                        }
                    }
                }
            }
        }
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log the token so you can copy it from Logcat
            Log.d("FCM", "FCM registration token: $token")
        }
    }
}
