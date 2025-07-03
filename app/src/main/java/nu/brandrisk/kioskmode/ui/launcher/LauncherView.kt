package nu.brandrisk.kioskmode.ui.launcher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import nu.brandrisk.kioskmode.R
import nu.brandrisk.kioskmode.ui.shared.AppIconItem
import nu.brandrisk.kioskmode.utils.Routes
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherView(
    navController: NavController,
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val apps = viewModel.apps.collectAsState(initial = emptyList())
    val imageLoader = viewModel.imageLoader
    val context = LocalContext.current
    val enabledApps = apps.value.filter { it.isEnabled && it.packageName != context.packageName }
    
    // Real-time clock state
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = getCurrentTime()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyGridState(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                EnterpriseHeader(
                    currentTime = currentTime,
                    enabledAppsCount = enabledApps.size,
                    onConfigClick = {
                        navController.navigate(Routes.ADMIN_PASSWORD)
                    }
                )
            }

            // Quick Actions
            item(span = { GridItemSpan(maxLineSpan) }) {
                QuickActionsSection(viewModel = viewModel)
            }

            // Apps Grid
            items(enabledApps) { app ->
                EnterpriseAppItem(
                    app = app,
                    imageLoader = imageLoader,
                    onClick = { viewModel.startApplication(app) }
                )
            }

            // Empty State
            if (enabledApps.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateMessage()
                }
            }
        }

        // Floating Admin Button
        FloatingActionButton(
            onClick = {
                navController.navigate(Routes.ADMIN_PASSWORD)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            backgroundColor = Color(0xFFE94560)
        ) {
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = "Admin",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun EnterpriseHeader(
    currentTime: String,
    enabledAppsCount: Int,
    onConfigClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🏢 Enterprise Kiosk",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Professional Mode Active",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(
                    onClick = onConfigClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time and Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = currentTime,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date()),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$enabledAppsCount Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "Available",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(viewModel: LauncherViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Wifi,
                    label = "WiFi",
                    onClick = { viewModel.openWiFiSettings() }
                )
                QuickActionButton(
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth",
                    onClick = { viewModel.openBluetoothSettings() }
                )
                QuickActionButton(
                    icon = Icons.Default.VolumeUp,
                    label = "Volume",
                    onClick = { viewModel.openVolumePanel() }
                )
                QuickActionButton(
                    icon = Icons.Default.Brightness6,
                    label = "Brightness",
                    onClick = { viewModel.openBrightnessSettings() }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EnterpriseAppItem(
    app: nu.brandrisk.kioskmode.data.model.App,
    imageLoader: coil.ImageLoader,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        AppIconItem(
            modifier = Modifier.padding(8.dp),
            app = app,
            imageLoader = imageLoader
        )
    }
}

@Composable
private fun EmptyStateMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Apps,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Apps Enabled",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Configure apps in settings to get started",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

private fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
