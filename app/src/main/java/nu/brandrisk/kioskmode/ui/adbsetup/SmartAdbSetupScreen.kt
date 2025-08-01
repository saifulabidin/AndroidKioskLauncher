package nu.brandrisk.kioskmode.ui.adbsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import nu.brandrisk.kioskmode.ui.config.ConfigViewModel

/**
 * Smart ADB Setup Screen
 * Automatically detects account status and provides appropriate guidance
 */
@Composable
fun SmartAdbSetupScreen(
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    var deviceOwnerStatus by remember { mutableStateOf(viewModel.checkDeviceOwnerEligibility()) }
    var existingAccounts by remember { mutableStateOf(viewModel.getExistingAccountsList()) }
    
    // Refresh status
    LaunchedEffect(Unit) {
        deviceOwnerStatus = viewModel.checkDeviceOwnerEligibility()
        existingAccounts = viewModel.getExistingAccountsList()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Adb,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "🔧 Device Owner Setup",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Smart Configuration Assistant",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status-based content
        when (deviceOwnerStatus) {
            ConfigViewModel.DeviceOwnerStatus.ALREADY_DEVICE_OWNER -> {
                AlreadyDeviceOwnerCard()
            }
            ConfigViewModel.DeviceOwnerStatus.ACCOUNTS_EXIST -> {
                AccountRemovalGuideCard(
                    accounts = existingAccounts,
                    onCopyCommand = { command ->
                        clipboardManager.setText(AnnotatedString(command))
                    }
                )
            }
            ConfigViewModel.DeviceOwnerStatus.READY_FOR_SETUP -> {
                StandardSetupCard(
                    onCopyCommand = { command ->
                        clipboardManager.setText(AnnotatedString(command))
                    }
                )
            }
            ConfigViewModel.DeviceOwnerStatus.ERROR -> {
                ErrorCard()
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Refresh button
        Button(
            onClick = {
                deviceOwnerStatus = viewModel.checkDeviceOwnerEligibility()
                existingAccounts = viewModel.getExistingAccountsList()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh Status")
        }
    }
}

@Composable
fun AlreadyDeviceOwnerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✅ Device Owner Active",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your device is already configured as device owner. You can now use all kiosk mode features!",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "💡 Available features: Lock task mode, app restrictions, boot automation, and advanced security policies.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AccountRemovalGuideCard(
    accounts: List<String>,
    onCopyCommand: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚠️ Accounts Detected",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFFF9800)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Device has existing accounts that prevent device owner setup:",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show existing accounts
            LazyColumn(
                modifier = Modifier.height(100.dp)
            ) {
                items(accounts) { account ->
                    Text(
                        text = "• $account",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Solution options
            Text("🔧 Solution Options:", fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Option 1: ADB Commands
            SolutionOption(
                title = "Option 1: ADB Commands (Quick)",
                description = "Remove accounts via ADB",
                commands = listOf(
                    "adb shell pm disable-user --user 0 com.google.android.gms",
                    "adb shell pm clear com.android.providers.contacts",
                    "adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver"
                ),
                onCopyCommand = onCopyCommand
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Option 2: Manual
            SolutionOption(
                title = "Option 2: Manual Removal",
                description = "Settings > Accounts > Remove all accounts",
                commands = listOf("Settings > Accounts & sync > Remove all accounts"),
                isManual = true,
                onCopyCommand = onCopyCommand
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Option 3: Factory Reset
            SolutionOption(
                title = "Option 3: Factory Reset (Safest)",
                description = "Complete device reset",
                commands = listOf("Settings > System > Reset options > Erase all data"),
                isManual = true,
                onCopyCommand = onCopyCommand
            )
        }
    }
}

@Composable
fun StandardSetupCard(
    onCopyCommand: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🚀 Ready for Setup",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2196F3)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No conflicting accounts detected. You can proceed with device owner setup:",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Setup command
            CommandCard(
                title = "Device Owner Setup Command",
                command = "adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver",
                onCopy = onCopyCommand
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Disable command
            CommandCard(
                title = "Disable Device Owner Command",
                command = "adb shell dpm remove-active-admin nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver",
                onCopy = onCopyCommand
            )
        }
    }
}

@Composable
fun SolutionOption(
    title: String,
    description: String,
    commands: List<String>,
    isManual: Boolean = false,
    onCopyCommand: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            commands.forEach { command ->
                if (isManual) {
                    Text(
                        text = command,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF1976D2)
                    )
                } else {
                    CommandCard(
                        command = command,
                        onCopy = onCopyCommand,
                        compact = true
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun CommandCard(
    title: String? = null,
    command: String,
    onCopy: (String) -> Unit,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(if (compact) 8.dp else 12.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 12.sp else 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = command,
                    fontFamily = FontFamily.Monospace,
                    fontSize = if (compact) 10.sp else 12.sp,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { onCopy(command) },
                    modifier = Modifier.size(if (compact) 24.dp else 32.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(if (compact) 16.dp else 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336).copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "❌ Error",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFF44336)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Unable to check device owner status. Please ensure:",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("• USB debugging is enabled", fontSize = 12.sp)
            Text("• ADB is properly connected", fontSize = 12.sp)
            Text("• Device has proper permissions", fontSize = 12.sp)
        }
    }
}
