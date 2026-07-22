package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppStatus
import com.example.domain.model.StoreConnectionStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(status: AppStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        AppStatus.LIVE -> Triple(SuccessBg, StatusLive, "Live")
        AppStatus.IN_REVIEW -> Triple(InfoBg, StatusInReview, "In Review")
        AppStatus.DRAFT -> Triple(WarningBg, StatusDraft, "Draft")
        AppStatus.ACTION_REQUIRED -> Triple(WarningBg, StatusActionRequired, "Action Required")
        AppStatus.REJECTED -> Triple(ErrorBg, StatusRejected, "Rejected")
        AppStatus.CLOSED -> Triple(CardSurfaceVariant, StatusClosed, "Closed")
        AppStatus.UNKNOWN -> Triple(CardSurfaceVariant, TextSecondary, "Unknown")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusBadgePreview() {
    PlayConsoleDashboardTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(status = AppStatus.LIVE)
            StatusBadge(status = AppStatus.IN_REVIEW)
            StatusBadge(status = AppStatus.REJECTED)
        }
    }
}

@Composable
fun StoreStatusPill(status: StoreConnectionStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        StoreConnectionStatus.CONNECTED -> Triple(SuccessBg, StatusLive, "Kết nối")
        StoreConnectionStatus.CONNECTION_ERROR -> Triple(ErrorBg, StatusRejected, "Lỗi kết nối")
        StoreConnectionStatus.SYNCING -> Triple(InfoBg, StatusInReview, "Đang đồng bộ")
        StoreConnectionStatus.UNCONFIGURED -> Triple(WarningBg, StatusDraft, "Chưa cấu hình")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StoreStatusPillPreview() {
    PlayConsoleDashboardTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StoreStatusPill(status = StoreConnectionStatus.CONNECTED)
            StoreStatusPill(status = StoreConnectionStatus.CONNECTION_ERROR)
            StoreStatusPill(status = StoreConnectionStatus.SYNCING)
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavigationItem("dashboard", "Tổng quan", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        NavigationItem("stores", "Stores", Icons.Outlined.Store, Icons.Filled.Store),
        NavigationItem("apps", "Ứng dụng", Icons.Outlined.Apps, Icons.Filled.Apps),
        NavigationItem("settings", "Cài đặt", Icons.Outlined.Settings, Icons.Filled.Settings)
    )

    NavigationBar(
        containerColor = CardSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (selected) PrimaryBlue else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (selected) PrimaryBlue else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = CardSurfaceVariant
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    PlayConsoleDashboardTheme {
        BottomNavigationBar(currentRoute = "dashboard", onNavigate = {})
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)
