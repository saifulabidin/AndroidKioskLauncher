package nu.brandrisk.kioskmode.ui.admin

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import nu.brandrisk.kioskmode.utils.Routes
import nu.brandrisk.kioskmode.ui.theme.KioskModeTheme

/**
 * Enterprise Admin Password Screen
 * Professional password entry with security features
 * Default password: "0000"
 */
@Composable
fun AdminPasswordScreen(
    navController: NavController,
    viewModel: AdminPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Auto-focus on password field
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // Handle successful authentication
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            navController.navigate(Routes.CONFIG) {
                popUpTo(Routes.LAUNCHER) { inclusive = false }
            }
        }
    }
    
    KioskModeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFE94560)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "🔐 Admin Access",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = "Enter admin password to continue",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        // Show default password hint if using default
                        if (uiState.isUsingDefaultPassword) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                backgroundColor = Color(0xFFFF9800).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "💡 Default password: 0000",
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 12.sp,
                                    color = Color(0xFFFF9800),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Password Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color.White,
                    elevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                viewModel.clearError()
                            },
                            label = { Text("Admin Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (password.isNotEmpty() && !uiState.isLockedOut) {
                                        isLoading = true
                                        viewModel.verifyPassword(password) { success ->
                                            isLoading = false
                                            if (!success) {
                                                password = ""
                                            }
                                        }
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            enabled = !uiState.isLockedOut && !isLoading,
                            isError = uiState.errorMessage != null
                        )
                        
                        // Error message
                        if (uiState.errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colors.error,
                                fontSize = 12.sp
                            )
                        }
                        
                        // Lockout message
                        if (uiState.isLockedOut) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                backgroundColor = Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🚫 Too many failed attempts. Locked for ${uiState.lockoutTimeRemaining / 1000 / 60} minutes.",
                                    modifier = Modifier.padding(12.dp),
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // Attempts remaining
                        if (!uiState.isLockedOut && uiState.failedAttempts > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ ${uiState.maxAttempts - uiState.failedAttempts} attempts remaining",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Login Button
                        Button(
                            onClick = {
                                if (password.isNotEmpty() && !uiState.isLockedOut) {
                                    isLoading = true
                                    viewModel.verifyPassword(password) { success ->
                                        isLoading = false
                                        if (!success) {
                                            password = ""
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = password.isNotEmpty() && !uiState.isLockedOut && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFE94560)
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
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Access Admin Panel", color = Color.White)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Back to Launcher
                TextButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Back to Launcher",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
