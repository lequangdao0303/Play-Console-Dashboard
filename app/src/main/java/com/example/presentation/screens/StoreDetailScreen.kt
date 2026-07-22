package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppItem
import com.example.presentation.MainViewModel
import com.example.presentation.components.StatusBadge
import com.example.presentation.components.StoreStatusPill
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    storeId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToStoreSettings: (String) -> Unit,
    onNavigateToAppDetail: (String) -> Unit
) {
    val stores by viewModel.stores.collectAsState()
    val allApps by viewModel.apps.collectAsState()
    val store = stores.find { it.id == storeId } ?: stores.firstOrNull()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddAppDialog by remember { mutableStateOf(false) }
    
    val storeApps = allApps.filter { it.storeId == (store?.id ?: storeId) }

    if (showAddAppDialog) {
        var packageName by remember { mutableStateOf("") }
        var displayName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            title = { Text("Thêm Ứng dụng") },
            text = {
                Column {
                    Text("Lưu ý: Bạn phải thêm chính xác Package Name để hệ thống đồng bộ dữ liệu.", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = { packageName = it },
                        label = { Text("Package Name") },
                        placeholder = { Text("com.example.app") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Tên hiển thị") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            },
            containerColor = CardSurface,
            titleContentColor = TextPrimary,
            confirmButton = {
                TextButton(onClick = {
                    val pkg = packageName.trim()
                    if (pkg.isNotBlank() && !pkg.contains(" ")) {
                        viewModel.addApp(storeId, pkg, displayName.trim().ifBlank { pkg })
                        showAddAppDialog = false
                    }
                }) {
                    Text("Thêm", color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(store?.name ?: "Chi tiết Store", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { store?.let { onNavigateToStoreSettings(it.id) } }) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { showAddAppDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = DarkBackground
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm App")
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header Info Card
            if (store != null) {
                Card(
            
        shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDA251D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(store.country ?: "VN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(store.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(store.serviceAccountEmail ?: "vnstore@company.com", color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StoreStatusPill(status = store.connectionStatus)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đồng bộ lần cuối: 2 phút trước", color = TextTertiary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs: "Tổng quan" / "Cài đặt"
                TabRow(
                    selectedTabIndex = selectedTabIndex,
        containerColor = DarkBackground,
                    contentColor = PrimaryBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = PrimaryBlue
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Tổng quan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                            onNavigateToStoreSettings(store.id)
                        },
                        text = { Text("Cài đặt", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatCard("Ứng dụng", store.appCount.toString(), Icons.Default.Apps, PrimaryBlue, Modifier.weight(1f))
                    MiniStatCard("Phiên bản", store.releaseCount.toString(), Icons.Default.Layers, SecondaryPurple, Modifier.weight(1f))
                    MiniStatCard("Live", store.liveCount.toString(), Icons.Default.CheckCircle, StatusLive, Modifier.weight(1f))
                    MiniStatCard("Cảnh báo", store.alertCount.toString(), Icons.Default.Warning, StatusActionRequired, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hoạt động gần đây", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Xem tất cả", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(storeApps) { app ->
                        Card(
                    
        shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAppDetail(app.packageName) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Đã phát hành lên Production", color = TextSecondary, fontSize = 12.sp)
                                }
                                StatusBadge(status = app.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(

        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(title, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
