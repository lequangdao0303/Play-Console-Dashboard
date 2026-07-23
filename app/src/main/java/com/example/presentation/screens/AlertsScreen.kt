package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import com.example.domain.model.AlertType
import com.example.domain.model.AppAlert
import com.example.presentation.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val alerts by viewModel.alerts.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredAlerts = alerts.filter { alert ->
        when (selectedTab) {
            1 -> alert.type == AlertType.CONNECTION_ERROR
            2 -> alert.type == AlertType.ACTION_REQUIRED || alert.type == AlertType.REJECTED
            3 -> alert.type == AlertType.INFO || alert.type == AlertType.QUOTA_WARNING
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cảnh báo", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = DarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo cảnh báo thủ công")
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
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
        containerColor = DarkBackground,
                contentColor = PrimaryBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryBlue
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tất cả", fontSize = 12.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Lỗi kết nối", fontSize = 12.sp) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Cần xử lý", fontSize = 12.sp) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Khác", fontSize = 12.sp) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAlerts) { alert ->
                    AlertCardItem(
                        alert = alert,
                        onMarkRead = { viewModel.markAlertRead(alert.id) },
                        onDelete = { viewModel.deleteAlert(alert.id) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddManualAlertBottomSheet(
            onDismiss = { showAddDialog = false },
            onAddAlert = { title, message, type, appId ->
                viewModel.addManualAlert(title, message, type, "store_vn", appId)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AlertCardItem(
    alert: AppAlert,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, color) = when (alert.type) {
        AlertType.CONNECTION_ERROR -> Icons.Default.Error to StatusRejected
        AlertType.REJECTED -> Icons.Default.Error to StatusRejected
        AlertType.ACTION_REQUIRED -> Icons.Default.Warning to StatusActionRequired
        AlertType.QUOTA_WARNING -> Icons.Default.Warning to StatusDraft
        else -> Icons.Default.Info to PrimaryBlue
    }

    Card(

        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkRead() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(alert.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(alert.message, color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(com.example.utils.DateUtils.getRelativeTime(alert.createdAt), color = TextTertiary, fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddManualAlertBottomSheet(
    onDismiss: () -> Unit,
    onAddAlert: (String, String, AlertType, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AlertType.REJECTED) }
    var appId by remember { mutableStateOf("com.company.pdf") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("Tạo cảnh báo thủ công", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề (ví dụ: Ứng dụng bị từ chối)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Lý do / Nội dung chi tiết") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onAddAlert(title, message, selectedType, appId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
        
        shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu cảnh báo", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
