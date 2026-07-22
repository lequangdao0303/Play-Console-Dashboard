package com.example.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppStatus
import com.example.domain.model.DashboardSummary
import com.example.domain.model.RecentActivity
import com.example.presentation.MainViewModel
import com.example.presentation.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToStores: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAppDetail: (String) -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsState()

    DashboardScreenContent(
        summary = summary,
        onNavigateToStores = onNavigateToStores,
        onNavigateToApps = onNavigateToApps,
        onNavigateToAlerts = onNavigateToAlerts,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToAppDetail = onNavigateToAppDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenContent(
    summary: DashboardSummary,
    onNavigateToStores: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAppDetail: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tổng quan",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = onNavigateToAlerts) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = TextPrimary
                            )
                        }
                        if (summary.alertCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(StatusRejected),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = summary.alertCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
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
            // 4 Metrics Grid Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Stores",
                        value = summary.totalStores.toString(),
                        increase = "+${summary.storeIncrease}",
                        icon = Icons.Default.Store,
                        iconTint = PrimaryBlue,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToStores() }
                    )
                    MetricCard(
                        title = "Ứng dụng",
                        value = summary.totalApps.toString(),
                        increase = "+${summary.appIncrease}",
                        icon = Icons.Default.Apps,
                        iconTint = StatusLive,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToApps() }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Phiên bản",
                        value = summary.totalReleases.toString(),
                        increase = "+${summary.releaseIncrease}",
                        icon = Icons.Default.Layers,
                        iconTint = SecondaryPurple,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToStatistics() }
                    )
                    MetricCard(
                        title = "Cảnh báo",
                        value = summary.alertCount.toString(),
                        increase = "+${summary.alertIncrease}",
                        icon = Icons.Default.Warning,
                        iconTint = StatusActionRequired,
                        isWarning = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToAlerts() }
                    )
                }
            }

            // Donut Chart & Status Breakdown
            item {
                Card(
            
        shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Trạng thái ứng dụng",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Xem tất cả",
                                color = PrimaryBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onNavigateToApps() }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Donut Chart
                            Box(
                                modifier = Modifier.size(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DonutChart(summary = summary)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = summary.totalApps.toString(),
                                        color = TextPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ứng dụng",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Legend breakdown
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val total = if (summary.totalApps > 0) summary.totalApps else 1
                                StatusLegendItem("Live", summary.liveCount, summary.liveCount * 100 / total, StatusLive)
                                StatusLegendItem("In Review", summary.inReviewCount, summary.inReviewCount * 100 / total, StatusInReview)
                                StatusLegendItem("Draft", summary.draftCount, summary.draftCount * 100 / total, StatusDraft)
                                StatusLegendItem("Rejected", summary.rejectedCount, summary.rejectedCount * 100 / total, StatusRejected)
                                StatusLegendItem("Action Required", summary.actionRequiredCount, summary.actionRequiredCount * 100 / total, StatusActionRequired)
                                StatusLegendItem("Closed", summary.closedCount, summary.closedCount * 100 / total, StatusClosed)
                            }
                        }
                    }
                }
            }

            // Recent Activity Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Hoạt động gần đây",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Xem tất cả",
                        color = PrimaryBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToApps() }
                    )
                }
            }

            items(summary.recentActivities) { activity ->
                RecentActivityCard(activity = activity, onClick = { onNavigateToAppDetail(activity.packageName) })
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    increase: String,
    icon: ImageVector,
    iconTint: Color,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(

        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background((if (isWarning) StatusActionRequired else StatusLive).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = increase,
                        color = if (isWarning) StatusActionRequired else StatusLive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusLegendItem(label: String, count: Int, percentage: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
        }
        Text(
            text = "$count ($percentage%)",
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DonutChart(summary: DashboardSummary) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val total = if (summary.totalApps > 0) summary.totalApps.toFloat() else 1f
        val sweepAngles = listOf(
            (summary.liveCount / total) * 360f,
            (summary.inReviewCount / total) * 360f,
            (summary.draftCount / total) * 360f,
            (summary.rejectedCount / total) * 360f,
            (summary.actionRequiredCount / total) * 360f,
            (summary.closedCount / total) * 360f
        )
        val colors = listOf(
            StatusLive,
            StatusInReview,
            StatusDraft,
            StatusRejected,
            StatusActionRequired,
            StatusClosed
        )

        var startAngle = -90f
        val strokeWidth = 24.dp.toPx()

        for (i in sweepAngles.indices) {
            val sweep = if (sweepAngles[i] < 5f && sweepAngles[i] > 0f) 5f else sweepAngles[i]
            drawArc(
                color = colors[i],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun RecentActivityCard(activity: RecentActivity, onClick: () -> Unit) {
    Card(

        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.appName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activity.actionText,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "2 phút trước",
                color = TextTertiary,
                fontSize = 11.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PlayConsoleDashboardTheme {
        DashboardScreenContent(
            summary = DashboardSummary(
                totalStores = 12,
                storeIncrease = 2,
                totalApps = 45,
                appIncrease = 5,
                totalReleases = 128,
                releaseIncrease = 12,
                alertCount = 3,
                alertIncrease = 1,
                liveCount = 30,
                inReviewCount = 5,
                draftCount = 4,
                rejectedCount = 2,
                actionRequiredCount = 2,
                closedCount = 2,
                recentActivities = listOf(
                    RecentActivity("1", "App 1", "com.app1", "Updated", 0, AppStatus.LIVE),
                    RecentActivity("2", "App 2", "com.app2", "Rejected", 0, AppStatus.REJECTED)
                )
            ),
            onNavigateToStores = {},
            onNavigateToApps = {},
            onNavigateToAlerts = {},
            onNavigateToStatistics = {},
            onNavigateToAppDetail = {}
        )
    }
}
