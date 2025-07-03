package nu.brandrisk.kioskmode.ui.changepassword

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import nu.brandrisk.kioskmode.ui.theme.KioskModeTheme

@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val currentPasswordFocusRequester = remember { FocusRequester() }
    val newPasswordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = true) {
        currentPasswordFocusRequester.requestFocus()
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
            if (uiState.isSuccess) {
                navController.navigateUp()
            }
        }
    }

    KioskModeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a1a2e),
                            Color(0xFF16213e),
                            Color(0xFF0f3460)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header
                Card(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Change Admin Password",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Update your admin panel access password",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Password Change Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color.White,
                    elevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Current Password
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { 
                                currentPassword = it
                                viewModel.clearMessage()
                            },
                            label = { Text("Current Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                                    Icon(
                                        if (isCurrentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isCurrentPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(currentPasswordFocusRequester),
                            enabled = !isLoading,
                            isError = uiState.error != null && uiState.error!!.contains("current", ignoreCase = true)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // New Password
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { 
                                newPassword = it
                                viewModel.clearMessage()
                            },
                            label = { Text("New Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                    Icon(
                                        if (isNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isNewPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(newPasswordFocusRequester),
                            enabled = !isLoading,
                            isError = uiState.error != null && uiState.error!!.contains("new", ignoreCase = true)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                viewModel.clearMessage()
                            },
                            label = { Text("Confirm New Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(
                                        if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                                        isLoading = true
                                        viewModel.changePassword(currentPassword, newPassword, confirmPassword) {
                                            isLoading = false
                                        }
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(confirmPasswordFocusRequester),
                            enabled = !isLoading,
                            isError = uiState.error != null && uiState.error!!.contains("confirm", ignoreCase = true)
                        )
                        
                        // Error message
                        uiState.error?.let { errorMessage ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colors.error,
                                fontSize = 12.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Change Password Button
                        Button(
                            onClick = {
                                if (currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                                    isLoading = true
                                    viewModel.changePassword(currentPassword, newPassword, confirmPassword) {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Change Password", color = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Reset to Default Button
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                viewModel.resetToDefault {
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF9800)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset to Default (1234)")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Back Button
                TextButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Admin", color = Color.White)
                }
            }
        }
    }
}
