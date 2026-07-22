package com.example.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppStatus
import com.example.presentation.MainViewModel
import com.example.presentation.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseHistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToReleaseDetail: (String, String, Int) -> Unit
) {
    var selectedAppFilter by remember { mutableStateOf("Tất cả ứng dụng") }
    var selectedTrackFilter by remember { mutableStateOf("Tất cả track") }

    val historyItems = listOf(
        HistoryReleaseItem("com.company.camera", "Camera Pro", "2.1.0", 120, "Production", "21/06/2024 12:30", AppStatus.LIVE),
        HistoryReleaseItem("com.company.chat", "AI Chat", "3.0.1", 31, "Open Testing", "21/06/2024 10:15", AppStatus.IN_REVIEW),
        HistoryReleaseItem("com.company.photo", "Photo Editor", "1.5.4", 54, "Production", "20/06/2024 09:20", AppStatus.LIVE),
        HistoryReleaseItem("com.company.video", "Video Maker", "1.0.3", 15, "Internal Testing", "19/06/2024 18:10", AppStatus.DRAFT),
        HistoryReleaseItem("com.company.pdf", "PDF Reader", "2.0.0", 22, "Production", "18/06/2024 16:45", AppStatus.REJECTED)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử phát hành", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Dropdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { },
            
        shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedAppFilter, fontSize = 12.sp)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                OutlinedButton(
                    onClick = { },
            
        shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedTrackFilter, fontSize = 12.sp)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyItems) { item ->
                    Card(
                
        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToReleaseDetail(item.packageName, item.trackName, item.versionCode) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.appName} v${item.versionName} (${item.versionCode})",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${item.trackName} • ${item.timeStr}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            StatusBadge(status = item.status)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

private data class HistoryReleaseItem(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val trackName: String,
    val timeStr: String,
    val status: AppStatus
)
