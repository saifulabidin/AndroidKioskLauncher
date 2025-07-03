package nu.brandrisk.kioskmode.ui.bootconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import nu.brandrisk.kioskmode.domain.enterprise.EnterpriseBootManager
import nu.brandrisk.kioskmode.ui.theme.KioskModeTheme

@Composable
fun BootConfigurationScreen(
    navController: NavController,
    viewModel: BootConfigurationViewModel = hiltViewModel()
) {
    val bootStatus by viewModel.bootStatus.collectAsState()
    var selectedStartupMode by remember { mutableStateOf(bootStatus.startupMode) }
    var bootDelay by remember { mutableStateOf((bootStatus.bootDelay / 1000).toString()) }
    var persistentMode by remember { mutableStateOf(bootStatus.isPersistent) }

    KioskModeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🚀 Enterprise Boot Configuration") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp,
                        backgroundColor = if (bootStatus.isEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (bootStatus.isEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (bootStatus.isEnabled) "Enterprise Auto-Start: ACTIVE" else "Enterprise Auto-Start: INACTIVE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            if (bootStatus.isDeviceOwner) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Device Owner Mode ✓", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Main Toggle
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⚙️ Enterprise Auto-Start", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Enable auto-start on device boot")
                                    Text(
                                        "Automatically launch kiosk mode when device boots",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = bootStatus.isEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled) {
                                            viewModel.enableAutoStart(selectedStartupMode, bootDelay.toLongOrNull()?.times(1000) ?: 3000L, persistentMode)
                                        } else {
                                            viewModel.disableAutoStart()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Startup Mode Configuration
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🎯 Startup Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            StartupModeOption(
                                mode = EnterpriseBootManager.Companion.StartupMode.KIOSK_IMMEDIATE,
                                title = "Immediate Kiosk Mode",
                                description = "Launch directly into kiosk mode (Recommended)",
                                selected = selectedStartupMode == EnterpriseBootManager.Companion.StartupMode.KIOSK_IMMEDIATE,
                                onSelect = { selectedStartupMode = it }
                            )
                            
                            StartupModeOption(
                                mode = EnterpriseBootManager.Companion.StartupMode.LAUNCHER_ONLY,
                                title = "Launcher Mode",
                                description = "Set as default launcher only",
                                selected = selectedStartupMode == EnterpriseBootManager.Companion.StartupMode.LAUNCHER_ONLY,
                                onSelect = { selectedStartupMode = it }
                            )
                            
                            StartupModeOption(
                                mode = EnterpriseBootManager.Companion.StartupMode.SILENT,
                                title = "Silent Background",
                                description = "Start services silently in background",
                                selected = selectedStartupMode == EnterpriseBootManager.Companion.StartupMode.SILENT,
                                onSelect = { selectedStartupMode = it }
                            )
                            
                            StartupModeOption(
                                mode = EnterpriseBootManager.Companion.StartupMode.NORMAL,
                                title = "Normal Mode",
                                description = "Standard application launch",
                                selected = selectedStartupMode == EnterpriseBootManager.Companion.StartupMode.NORMAL,
                                onSelect = { selectedStartupMode = it }
                            )
                        }
                    }
                }

                // Advanced Settings
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⚡ Advanced Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Boot Delay
                            Text("Boot Delay (seconds)", fontWeight = FontWeight.Medium)
                            OutlinedTextField(
                                value = bootDelay,
                                onValueChange = { bootDelay = it },
                                label = { Text("Delay before startup") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Persistent Mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Persistent Mode", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Keep app running persistently (Device Owner required)",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = persistentMode,
                                    onCheckedChange = { persistentMode = it },
                                    enabled = bootStatus.isDeviceOwner
                                )
                            }
                        }
                    }
                }

                // Device Information
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📱 Device Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            DeviceStatusRow("Device Owner", if (bootStatus.isDeviceOwner) "✓ Active" else "✗ Inactive", bootStatus.isDeviceOwner)
                            DeviceStatusRow("Boot Receiver", if (bootStatus.bootReceiverEnabled) "✓ Enabled" else "✗ Disabled", bootStatus.bootReceiverEnabled)
                            DeviceStatusRow("Auto-Start", if (bootStatus.isEnabled) "✓ Configured" else "✗ Not Configured", bootStatus.isEnabled)
                        }
                    }
                }

                // Apply Button
                item {
                    Button(
                        onClick = {
                            if (bootStatus.isEnabled) {
                                viewModel.enableAutoStart(
                                    selectedStartupMode,
                                    bootDelay.toLongOrNull()?.times(1000) ?: 3000L,
                                    persistentMode
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = bootStatus.isEnabled
                    ) {
                        Text("Apply Configuration", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StartupModeOption(
    mode: EnterpriseBootManager.Companion.StartupMode,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: (EnterpriseBootManager.Companion.StartupMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelect(mode) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun DeviceStatusRow(label: String, value: String, isPositive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            value,
            color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5722),
            fontWeight = FontWeight.Medium
        )
    }
}
