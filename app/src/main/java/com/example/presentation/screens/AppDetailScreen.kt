package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppDetail
import com.example.domain.model.TrackRelease
import com.example.presentation.MainViewModel
import com.example.presentation.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToReleaseDetail: (String, String, Int) -> Unit
) {
    val appDetailState by viewModel.repository.getAppDetailFlow(packageName).collectAsState(initial = null)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appDetailState?.app?.displayName ?: "Chi tiết ứng dụng", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { appDetailState?.app?.storeId?.let { viewModel.syncStore(it) } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = DarkBackground
    ) { innerPadding ->
        if (appDetailState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            val app = appDetailState!!.app
            val tracks = appDetailState!!.tracks

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Header App Info
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(PrimaryBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(app.displayName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(app.packageName, color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(status = app.status)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trên ${app.mainTrack}", color = TextSecondary, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hiển thị trên Google Play", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs
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
                    Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Tổng quan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) })
                    Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Phiên bản", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) })
                    Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Chi tiết", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) })
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Group tracks
                    val prodTracks = tracks.filter { it.trackName.contains("Production", true) }
                    val testingTracks = tracks.filter { !it.trackName.contains("Production", true) }

                    item {
                        TrackHeaderSection(
                            trackTitle = "Production",
                            releases = if (prodTracks.isNotEmpty()) prodTracks else listOf(
                                TrackRelease("", app.packageName, "Production", app.latestVersionCode ?: 120, app.latestVersionName ?: "2.1.0", "completed", 1.0f, System.currentTimeMillis() - 7200000, "Cải thiện hiệu suất ứng dụng\nSửa lỗi crash khi quay video")
                            ),
                            onReleaseClick = { rel -> onNavigateToReleaseDetail(app.packageName, rel.trackName, rel.versionCode) }
                        )
                    }

                    item {
                        TrackHeaderSection(
                            trackTitle = "Open Testing",
                            releases = if (testingTracks.isNotEmpty()) testingTracks else listOf(
                                TrackRelease("", app.packageName, "Open Testing", 121, "2.1.0-beta.1", "inProgress", 0.5f, System.currentTimeMillis() - 3600000, "Thử nghiệm tính năng mới")
                            ),
                            onReleaseClick = { rel -> onNavigateToReleaseDetail(app.packageName, rel.trackName, rel.versionCode) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TrackHeaderSection(
    trackTitle: String,
    releases: List<TrackRelease>,
    onReleaseClick: (TrackRelease) -> Unit
) {
    Card(

        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(trackTitle, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Quản lý track", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            releases.forEach { release ->
                ReleaseRowItem(release = release, onClick = { onReleaseClick(release) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReleaseRowItem(release: TrackRelease, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardSurfaceVariant)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = StatusLive,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text("${release.versionName} (${release.versionCode})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("2 giờ trước", color = TextSecondary, fontSize = 11.sp)
        }

        Text("${(release.userFraction * 100).toInt()}%", color = StatusLive, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
