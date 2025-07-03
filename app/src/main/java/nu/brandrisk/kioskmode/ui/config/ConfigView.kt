package nu.brandrisk.kioskmode.ui.config

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nu.brandrisk.kioskmode.R
import nu.brandrisk.kioskmode.domain.HomeScreenSettings
import nu.brandrisk.kioskmode.ui.shared.AppIconItem
import nu.brandrisk.kioskmode.utils.UiEvent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConfigView(
    navController: NavController,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val apps = viewModel.apps.collectAsState(initial = emptyList())
    val imageLoader = viewModel.imageLoader
    var selectedTab by remember { mutableStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.enableKioskMode(context)
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Enterprise Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🏢 Enterprise Kiosk Manager",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Professional kiosk solution for Xiaomi devices",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // Tab Navigation
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Apps") },
                icon = { Icon(Icons.Default.Apps, contentDescription = "Apps") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Security") },
                icon = { Icon(Icons.Default.Security, contentDescription = "Security") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Network") },
                icon = { Icon(Icons.Default.Wifi, contentDescription = "Network") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Hardware") },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Hardware") }
            )
        }

        when (selectedTab) {
            0 -> AppsManagementTab(
                apps = apps.value,
                imageLoader = imageLoader,
                viewModel = viewModel,
                context = context,
                launcher = launcher
            )
            1 -> SecurityManagementTab(viewModel = viewModel, context = context)
            2 -> NetworkManagementTab(viewModel = viewModel, context = context)
            3 -> HardwareManagementTab(viewModel = viewModel, context = context)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppsManagementTab(
    apps: List<nu.brandrisk.kioskmode.data.model.App>,
    imageLoader: coil.ImageLoader,
    viewModel: ConfigViewModel,
    context: android.content.Context,
    launcher: androidx.activity.compose.ManagedActivityResultLauncher<android.content.Intent, androidx.activity.result.ActivityResult>
) {
    // Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // all, enabled, disabled
    
    // Filtered apps
    val filteredApps = remember(apps, searchQuery, selectedFilter) {
        apps.filter { app ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                app.label.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            }
            
            val matchesFilter = when (selectedFilter) {
                "enabled" -> app.isEnabled
                "disabled" -> !app.isEnabled
                else -> true
            }
            
            matchesSearch && matchesFilter
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyGridState()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Launcher Status
                if (!HomeScreenSettings().isThisAppTheHomeScreen(context)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colors.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Not set as default launcher", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { HomeScreenSettings().showSelectHomeScreen(context) },
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                            ) {
                                Text(text = stringResource(R.string.set_as_launcher), color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kiosk Mode Control
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Kiosk Mode Control", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (viewModel.isDeviceOwner(context)) {
                                        launcher.launch(viewModel.toggleKioskMode.getAddDeviceAdminIntent())
                                    } else {
                                        viewModel.showADBSetupScreen()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = stringResource(R.string.enable_kiosk_mode))
                            }

                            if (viewModel.isDeviceOwner(context)) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.disableKioskMode(context) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                                ) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = stringResource(R.string.disable_kiosk_mode))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bulk App Controls
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bulk App Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.enableAllApps() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = stringResource(R.string.enable_all_apps))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.disableAllApps() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336))
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = stringResource(R.string.disable_all_apps))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "📱 ${stringResource(R.string.enable_disable_apps)} (${apps.filter { it.isEnabled }.size}/${apps.size} enabled)",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Enhanced App Filtering
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🔍 App Filtering & Search", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Search functionality
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search apps...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Filter buttons
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                onClick = { selectedFilter = "all" },
                                label = { Text("All (${apps.size})") },
                                selected = selectedFilter == "all"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                onClick = { selectedFilter = "enabled" },
                                label = { Text("Enabled (${apps.count { it.isEnabled }})") },
                                selected = selectedFilter == "enabled"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                onClick = { selectedFilter = "disabled" },
                                label = { Text("Disabled (${apps.count { !it.isEnabled }})") },
                                selected = selectedFilter == "disabled"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category-based Management
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📂 Category Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val systemApps = apps.filter { isSystemApp(it.packageName, context) }
                        val userApps = apps.filter { !isSystemApp(it.packageName, context) }
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { 
                                    // Enable/disable all system apps
                                    viewModel.toggleSystemApps()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("System Apps (${systemApps.size})")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { 
                                    // Enable/disable all user apps
                                    viewModel.toggleUserApps()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9C27B0))
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("User Apps (${userApps.size})")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enterprise App Policies
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🏢 Enterprise Policies", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(modifier = Modifier.fillMaxWidth()) {
                            item {
                                Button(
                                    onClick = { viewModel.applyWhitelistPolicy() },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Apply Whitelist")
                                }
                            }
                            item { Spacer(modifier = Modifier.width(8.dp)) }
                            item {
                                Button(
                                    onClick = { viewModel.applyEducationPolicy() },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                                ) {
                                    Text("Education Mode")
                                }
                            }
                            item { Spacer(modifier = Modifier.width(8.dp)) }
                            item {
                                Button(
                                    onClick = { viewModel.applyKioskPolicy() },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE94560))
                                ) {
                                    Text("Kiosk Mode")
                                }
                            }
                            item { Spacer(modifier = Modifier.width(8.dp)) }
                            item {
                                Button(
                                    onClick = { viewModel.applyBusinessPolicy() },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF607D8B))
                                ) {
                                    Text("Business Mode")
                                }
                            }
                        }
                    }
                }
            }
        }

        items(filteredApps) { app ->
            EnhancedAppIconItem(
                app = app,
                imageLoader = imageLoader,
                isSystemApp = isSystemApp(app.packageName, context),
                onToggle = { viewModel.updateEnabledFlag(app) }
            )
        }
    }
}

@Composable
private fun SecurityManagementTab(
    viewModel: ConfigViewModel,
    context: android.content.Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔒 Security Features", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Device Admin Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Device Admin")
                        Switch(
                            checked = viewModel.isDeviceOwner(context),
                            onCheckedChange = { /* Handle device admin toggle */ }
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Security Options
                    SecurityOptionItem("Password Protection", "Set admin password", Icons.Default.Password)
                    SecurityOptionItem("Biometric Lock", "Fingerprint/Face unlock", Icons.Default.Fingerprint)
                    SecurityOptionItem("Session Timeout", "Auto-lock after inactivity", Icons.Default.Timer)
                    SecurityOptionItem("Screen Recording Block", "Prevent screenshots", Icons.Default.Block)
                }
            }
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🛡️ MIUI Security Integration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SecurityOptionItem("Second Space", "MIUI workspace isolation", Icons.Default.Workspaces)
                    SecurityOptionItem("App Lock Bypass", "Skip MIUI app locks", Icons.Default.LockOpen)
                    SecurityOptionItem("Security Center", "MIUI security policies", Icons.Default.Security)
                }
            }
        }
    }
}

@Composable
private fun NetworkManagementTab(
    viewModel: ConfigViewModel,
    context: android.content.Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📶 Network Controls", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NetworkOptionItem("WiFi Management", "Configure enterprise WiFi", Icons.Default.Wifi)
                    NetworkOptionItem("Mobile Data", "APN and data controls", Icons.Default.SimCard)
                    NetworkOptionItem("Bluetooth", "Device connectivity", Icons.Default.Bluetooth)
                    NetworkOptionItem("NFC", "Near field communication", Icons.Default.Nfc)
                    NetworkOptionItem("VPN", "Enterprise VPN setup", Icons.Default.VpnKey)
                }
            }
        }
    }
}

@Composable
private fun HardwareManagementTab(
    viewModel: ConfigViewModel,
    context: android.content.Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚙️ Hardware Controls", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HardwareOptionItem("Camera", "Control camera access", Icons.Default.Camera)
                    HardwareOptionItem("Microphone", "Audio recording controls", Icons.Default.Mic)
                    HardwareOptionItem("Display", "Brightness and rotation", Icons.Default.DisplaySettings)
                    HardwareOptionItem("Volume", "System audio controls", Icons.Default.VolumeUp)
                    HardwareOptionItem("Flashlight", "LED torch control", Icons.Default.Flashlight)
                }
            }
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔋 MIUI Optimizations", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HardwareOptionItem("Battery Whitelist", "Prevent app killing", Icons.Default.Battery6Bar)
                    HardwareOptionItem("Autostart Permission", "Boot optimization", Icons.Default.PowerSettingsNew)
                    HardwareOptionItem("Game Turbo", "Performance mode", Icons.Default.Speed)
                }
            }
        }
    }
}

@Composable
private fun SecurityOptionItem(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun NetworkOptionItem(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun HardwareOptionItem(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

/**
 * Helper function to check if an app is a system app
 */
private fun isSystemApp(packageName: String, context: Context): Boolean {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        (packageInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
    } catch (e: Exception) {
        false
    }
}

/**
 * Enhanced App Icon with status indicators
 */
@Composable
private fun EnhancedAppIconItem(
    app: nu.brandrisk.kioskmode.data.model.App,
    imageLoader: coil.ImageLoader,
    isSystemApp: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .size(120.dp)
            .clickable { onToggle() },
        elevation = if (app.isEnabled) 4.dp else 1.dp,
        backgroundColor = if (app.isEnabled) Color.White else Color.Gray.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(app.packageName)
                        .transformations(CoilAppIconFetcher.AppIconTransformation())
                        .build(),
                    contentDescription = stringResource(R.string.icon_of_string, app.label),
                    imageLoader = imageLoader,
                    modifier = Modifier.size(48.dp)
                )
                
                // Status indicators
                if (isSystemApp) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "System App",
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.Blue, CircleShape)
                            .padding(2.dp),
                        tint = Color.White
                    )
                }
                
                if (!app.isEnabled) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = "Disabled",
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Red, CircleShape)
                            .padding(2.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = app.label,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (app.isEnabled) Color.Black else Color.Gray
            )
        }
    }
}