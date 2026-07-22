package com.example.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val dailySnapshots by viewModel.dailySnapshots.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Picker Button
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("14/06/2024 - 21/06/2024", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryBlue)
                    }
                }
            }

            // Stat Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Ứng dụng Live", summary.liveCount.toString(), "+12%", StatusLive, Modifier.weight(1f))
                    StatCard("Ứng dụng In Review", summary.inReviewCount.toString(), "-5%", StatusInReview, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Ứng dụng Rejected", summary.rejectedCount.toString(), "-2%", StatusRejected, Modifier.weight(1f))
                    StatCard("Tổng phiên bản", summary.totalReleases.toString(), "+8%", SecondaryPurple, Modifier.weight(1f))
                }
            }

            // Line Chart
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Biểu đồ trạng thái ứng dụng", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Multi-line chart canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            MultiLineTrendChart()
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chart dates axis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("14/06", "16/06", "18/06", "20/06", "21/06").forEach { d ->
                                Text(d, color = TextTertiary, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LegendDot("Live", StatusLive)
                            LegendDot("In Review", StatusInReview)
                            LegendDot("Draft", StatusDraft)
                            LegendDot("Rejected", StatusRejected)
                            LegendDot("Closed", StatusClosed)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, change: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(change, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun MultiLineTrendChart() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val livePoints = listOf(0.4f, 0.45f, 0.5f, 0.52f, 0.6f)
        val inReviewPoints = listOf(0.2f, 0.25f, 0.22f, 0.3f, 0.28f)
        val draftPoints = listOf(0.15f, 0.18f, 0.15f, 0.12f, 0.14f)
        val rejectedPoints = listOf(0.1f, 0.08f, 0.12f, 0.1f, 0.08f)

        fun drawTrendPath(points: List<Float>, color: Color) {
            val path = Path()
            points.forEachIndexed { i, p ->
                val x = (i.toFloat() / (points.size - 1)) * width
                val y = height - (p * height)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx()))
        }

        drawTrendPath(livePoints, StatusLive)
        drawTrendPath(inReviewPoints, StatusInReview)
        drawTrendPath(draftPoints, StatusDraft)
        drawTrendPath(rejectedPoints, StatusRejected)
    }
}
