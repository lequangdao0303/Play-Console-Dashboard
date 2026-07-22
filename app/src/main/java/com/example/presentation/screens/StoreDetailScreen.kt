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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.utils.DateUtils

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
                    IconButton(onClick = { store?.let { viewModel.syncStore(it.id) } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Đồng bộ", tint = PrimaryBlue)
                    }
                    IconButton(onClick = { store?.let { onNavigateToStoreSettings(it.id) } }) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAppDialog = true },
                containerColor = PrimaryBlue,
                contentColor = DarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm Ứng dụng")
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
                        Text(store.serviceAccountEmail ?: "N/A", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StoreStatusPill(status = store.connectionStatus)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đồng bộ: ${store.lastSyncAt?.let { DateUtils.formatDateTime(it) } ?: "Chưa đồng bộ"}", color = TextTertiary, fontSize = 11.sp)
                        }
                    }
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
                    Text("Danh sách Ứng dụng", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Add padding so it scrolls past FAB
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
                                    if (app.iconUrl != null) {
                                        AsyncImage(
                                            model = app.iconUrl,
                                            contentDescription = "App Icon",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(app.packageName, color = TextSecondary, fontSize = 12.sp)
                                }
                                StatusBadge(status = app.status)
                                IconButton(onClick = { viewModel.deleteApp(app.packageName) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa App", tint = StatusRejected)
                                }
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
