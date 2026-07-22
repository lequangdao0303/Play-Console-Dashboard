package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToStores: () -> Unit,
    onNavigateToAddStore: () -> Unit
) {
    var showWipeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                Card(
            
        shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Admin", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("admin@company.com", color = TextSecondary, fontSize = 12.sp)
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }

            // Management Section
            item {
                Text("Quản lý", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
            
        shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingRow(Icons.Default.Store, "Stores", onClick = onNavigateToStores)
                        Divider(color = CardBorder, thickness = 0.5.dp)
                        SettingRow(Icons.Default.Sync, "Lịch sử đồng bộ", onClick = {})
                        Divider(color = CardBorder, thickness = 0.5.dp)
                        SettingRow(Icons.Default.Notifications, "Thông báo", onClick = {})
                    }
                }
            }

            // System Section
            item {
                Text("Hệ thống", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
            
        shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingRow(Icons.Default.Publish, "Nhập Catalog Store (JSON)", onClick = onNavigateToAddStore)
                        Divider(color = CardBorder, thickness = 0.5.dp)
                        SettingRow(Icons.Default.History, "Nhật ký hoạt động", onClick = {})
                        Divider(color = CardBorder, thickness = 0.5.dp)

                        // Mandated Security Action from Section 2.1 & 13
                        SettingRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Xoá toàn bộ credential cục bộ",
                            iconTint = StatusRejected,
                            titleColor = StatusRejected,
                            onClick = { showWipeDialog = true }
                        )

                        Divider(color = CardBorder, thickness = 0.5.dp)
                        SettingRow(Icons.Default.Help, "Trợ giúp & FAQ", onClick = {})
                        Divider(color = CardBorder, thickness = 0.5.dp)
                        SettingRow(Icons.Default.Info, "Giới thiệu", onClick = {})
                    }
                }
            }

            // Logout Button
            item {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorBg),
            
        shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Đăng xuất", color = StatusRejected, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("Xoá Credential Cục bộ?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Thao tác này sẽ xoá toàn bộ Service Account keys đã lưu mã hoá trên máy này để bảo mật.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.wipeAllCredentials()
                        showWipeDialog = false
                    }
                ) {
                    Text("Xoá ngay", color = StatusRejected, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            },
            containerColor = CardSurface
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    iconTint: Color = TextPrimary,
    titleColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = titleColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}
