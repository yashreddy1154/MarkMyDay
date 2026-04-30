package com.example.markmyday2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.markmyday2.data.model.UserRole
import com.example.markmyday2.ui.viewmodel.AuthViewModel
import com.example.markmyday2.ui.viewmodel.UserState

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val userState by viewModel.userState.collectAsState()

    LaunchedEffect(userState) {
        if (userState is UserState.Authenticated) {
            onLoginSuccess((userState as UserState.Authenticated).user.role.name)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Attendance App Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (userState is UserState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }

        if (userState is UserState.Error) {
            Text(
                text = (userState as UserState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Text(text = "Demo Login (Bypass Firebase)", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { viewModel.loginAsDemo(UserRole.ADMIN) }) { Text("Admin") }
            OutlinedButton(onClick = { viewModel.loginAsDemo(UserRole.STUDENT) }) { Text("Student") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Demo Teachers", style = MaterialTheme.typography.labelSmall)
        
        val demoTeachers = listOf(
            "Dr. Smith" to "teacher_1",
            "Prof. Jones" to "teacher_2",
            "Ms. Davis" to "teacher_3",
            "Mr. Wilson" to "teacher_4",
            "Dr. Brown" to "teacher_5",
            "Ms. Taylor" to "teacher_6",
            "Prof. Clark" to "teacher_7"
        )

        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
            modifier = Modifier.height(150.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(demoTeachers.size) { index ->
                val (name, id) = demoTeachers[index]
                OutlinedButton(
                    onClick = { viewModel.loginAsDemo(UserRole.TEACHER, id, name) },
                    contentPadding = PaddingValues(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
    }
}
