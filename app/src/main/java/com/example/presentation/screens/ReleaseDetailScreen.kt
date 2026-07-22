package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.MainViewModel
import com.example.ui.theme.*
import com.example.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseDetailScreen(
    packageName: String,
    trackName: String,
    versionCode: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val appDetailState by viewModel.repository.getAppDetailFlow(packageName).collectAsState(initial = null)
    
    val release = appDetailState?.tracks?.find { it.trackName == trackName && it.versionCode == versionCode }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phiên bản ${release?.versionName ?: ""} ($versionCode)", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = DarkBackground
    ) { innerPadding ->
        if (release == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            val statusColor = when (release.releaseStatus) {
                "completed" -> StatusLive
                "inProgress" -> PrimaryBlue
                "halted" -> StatusRejected
                else -> TextSecondary
            }
            
            val statusIcon = when (release.releaseStatus) {
                "completed" -> Icons.Default.CheckCircle
                "inProgress" -> Icons.Default.Sync
                "halted" -> Icons.Default.Warning
                else -> Icons.Default.Info
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Header Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(release.releaseStatus.replaceFirstChar { it.uppercase() }, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Trên $trackName", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Cập nhật lần cuối: ${DateUtils.formatDateTime(release.updatedTime)}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // Version Information Table
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Thông tin phiên bản", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow("Version code", release.versionCode.toString())
                            InfoRow("Version name", release.versionName)
                            InfoRow("Trạng thái", release.releaseStatus)
                            InfoRow("Tỷ lệ phát hành", "${(release.userFraction * 100).toInt()}%")
                        }
                    }
                }

                // Release Notes
                if (!release.releaseNotes.isNullOrEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Ghi chú phát hành", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(release.releaseNotes, color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
