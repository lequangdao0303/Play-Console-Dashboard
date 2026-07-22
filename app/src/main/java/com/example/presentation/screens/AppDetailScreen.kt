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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.utils.DateUtils
import com.example.domain.model.AppItem

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
                            if (app.iconUrl != null) {
                                AsyncImage(
                                    model = app.iconUrl,
                                    contentDescription = "App Icon",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Smartphone, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                            }
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
                    when (selectedTabIndex) {
                        0 -> {
                            item {
                                OverviewTab(app = app, tracks = tracks)
                            }
                        }
                        1 -> {
                            val groupedTracks = tracks.groupBy { it.trackName }
                            if (groupedTracks.isEmpty()) {
                                item {
                                    Text("Chưa có phiên bản nào được phát hành", color = TextSecondary, fontSize = 14.sp)
                                }
                            } else {
                                groupedTracks.forEach { (trackName, trackReleases) ->
                                    item {
                                        TrackHeaderSection(
                                            trackTitle = trackName,
                                            releases = trackReleases,
                                            onReleaseClick = { rel -> onNavigateToReleaseDetail(app.packageName, rel.trackName, rel.versionCode) }
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            item {
                                DetailTab(app = app)
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun OverviewTab(app: AppItem, tracks: List<TrackRelease>) {
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Tổng quan thống kê", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Trạng thái", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(app.status.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tổng số track", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(tracks.distinctBy { it.trackName }.size.toString(), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Bản cập nhật mới nhất", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(app.latestVersionName ?: "N/A", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Mã phiên bản", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text((app.latestVersionCode ?: 0).toString(), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DetailTab(app: AppItem) {
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Chi tiết ứng dụng", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailRowItem("Package Name", app.packageName)
            Spacer(modifier = Modifier.height(12.dp))
            DetailRowItem("Store quản lý", app.storeName ?: app.storeId)
            Spacer(modifier = Modifier.height(12.dp))
            DetailRowItem("Cập nhật lần cuối", DateUtils.formatDateTime(app.lastUpdatedTime))
            
            if (app.rejectedReason != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailRowItem("Lý do từ chối", app.rejectedReason, isError = true)
            }
        }
    }
}

@Composable
fun DetailRowItem(label: String, value: String, isError: Boolean = false) {
    Column {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = if (isError) StatusRejected else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
            imageVector = statusIcon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${release.versionName} (${release.versionCode})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(DateUtils.formatDateTime(release.updatedTime), color = TextSecondary, fontSize = 11.sp)
            if (!release.releaseNotes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(release.releaseNotes.take(50) + if (release.releaseNotes.length > 50) "..." else "", color = TextTertiary, fontSize = 11.sp, maxLines = 1)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${(release.userFraction * 100).toInt()}%", color = statusColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(release.releaseStatus.replaceFirstChar { it.uppercase() }, color = statusColor, fontSize = 11.sp)
        }
    }
}
