package nu.brandrisk.kioskmode.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is nu.brandrisk.kioskmode.utils.UiEvent.ShowMessage -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
                is nu.brandrisk.kioskmode.utils.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
                is nu.brandrisk.kioskmode.utils.UiEvent.StartActivity -> {
                    // Not used in this screen, but required for exhaustive when
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Change Admin Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Password Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            backgroundColor = if (uiState.isUsingDefaultPassword) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (uiState.isUsingDefaultPassword) "⚠️ Warning" else "✅ Secure",
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isUsingDefaultPassword) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (uiState.isUsingDefaultPassword) 
                        "You are using the default password. Please change it for better security." 
                    else 
                        "Your admin password is secure.",
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Current Password Field
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current Password") },
            visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                    Icon(
                        imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (currentPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // New Password Field
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                    Icon(
                        imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm New Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword
        )

        // Password mismatch error
        if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colors.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Password Requirements
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Password Requirements:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val requirements = listOf(
                    "At least 6 characters long" to (newPassword.length >= 6),
                    "Contains uppercase letter" to newPassword.any { it.isUpperCase() },
                    "Contains lowercase letter" to newPassword.any { it.isLowerCase() },
                    "Contains number" to newPassword.any { it.isDigit() }
                )
                
                requirements.forEach { (requirement, met) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (met) "✅" else "❌",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = requirement,
                            fontSize = 12.sp,
                            color = if (met) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Change Password Button
        Button(
            onClick = {
                viewModel.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            },
            enabled = currentPassword.isNotEmpty() && 
                     newPassword.isNotEmpty() && 
                     confirmPassword.isNotEmpty() &&
                     newPassword == confirmPassword &&
                     newPassword.length >= 6 &&
                     newPassword.any { it.isUpperCase() } &&
                     newPassword.any { it.isLowerCase() } &&
                     newPassword.any { it.isDigit() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Text(
                text = "Change Password",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset to Default Button (for testing/emergency)
        if (uiState.isUsingDefaultPassword) {
            OutlinedButton(
                onClick = {
                    viewModel.resetToDefaultPassword()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Reset to Default Password",
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Error Message
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFFFEBEE),
                elevation = 2.dp
            ) {
                Text(
                    text = error,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
