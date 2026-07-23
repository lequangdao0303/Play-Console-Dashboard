package com.example.domain.model

import com.google.gson.annotations.SerializedName

enum class AppStatus(val displayName: String) {
    LIVE("Live"),
    IN_REVIEW("In Review"),
    DRAFT("Draft"),
    ACTION_REQUIRED("Action Required"),
    CLOSED("Closed"),
    REJECTED("Rejected"),
    UNKNOWN("Unknown")
}

enum class StoreConnectionStatus(val label: String) {
    CONNECTED("Kết nối"),
    CONNECTION_ERROR("Lỗi kết nối"),
    SYNCING("Đang đồng bộ"),
    UNCONFIGURED("Chưa cấu hình")
}

enum class AlertType(val label: String) {
    CONNECTION_ERROR("Lỗi kết nối"),
    SYNC_FAILED("Đồng bộ thất bại"),
    QUOTA_WARNING("Cảnh báo Quota"),
    ACTION_REQUIRED("Cần xử lý"),
    PERMISSION_ERROR("Lỗi truy cập"),
    REJECTED("Ứng dụng bị từ chối"),
    INFO("Thông tin")
}

data class Store(
    val id: String,
    val name: String,
    val country: String? = "VN",
    val serviceAccountEmail: String? = null,
    val hasCredential: Boolean = false,
    val connectionStatus: StoreConnectionStatus = StoreConnectionStatus.CONNECTED,
    val appCount: Int = 0,
    val releaseCount: Int = 0,
    val liveCount: Int = 0,
    val alertCount: Int = 0,
    val lastSyncAt: Long? = System.currentTimeMillis() - 120000,
    val autoSyncEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 15
)

data class AppItem(
    val packageName: String,
    val storeId: String,
    val storeName: String? = null,
    val displayName: String,
    val mainTrack: String = "Production",
    val status: AppStatus = AppStatus.LIVE,
    val latestVersionName: String? = "1.0.0",
    val latestVersionCode: Int? = 100,
    val userFraction: Float = 1.0f,
    val lastUpdatedTime: Long = System.currentTimeMillis() - 7200000,
    val hasManualRejectedAlert: Boolean = false,
    val rejectedReason: String? = null,
    val iconUrl: String? = null
)

data class TrackRelease(
    val id: String = "",
    val packageName: String,
    val trackName: String, // Production, Open Testing, Internal Testing
    val versionCode: Int,
    val versionName: String,
    val releaseStatus: String, // completed, inProgress, draft, halted
    val userFraction: Float = 1.0f,
    val updatedTime: Long = System.currentTimeMillis(),
    val releaseNotes: String? = null
)

data class AppDetail(
    val app: AppItem,
    val tracks: List<TrackRelease> = emptyList(),
    val storeName: String = ""
)

data class AppAlert(
    val id: String,
    val storeId: String,
    val storeName: String? = null,
    val appId: String? = null,
    val appName: String? = null,
    val title: String,
    val message: String,
    val type: AlertType,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isManual: Boolean = false
)

data class DashboardSummary(
    val totalStores: Int = 0,
    val storeIncrease: Int = 2,
    val totalApps: Int = 0,
    val appIncrease: Int = 6,
    val totalReleases: Int = 0,
    val releaseIncrease: Int = 2,
    val alertCount: Int = 0,
    val alertIncrease: Int = 1,
    val liveCount: Int = 0,
    val inReviewCount: Int = 0,
    val draftCount: Int = 0,
    val rejectedCount: Int = 0,
    val actionRequiredCount: Int = 0,
    val closedCount: Int = 0,
    val recentActivities: List<RecentActivity> = emptyList()
)

data class RecentActivity(
    val id: String,
    val appName: String,
    val packageName: String,
    val actionText: String,
    val timestamp: Long,
    val status: AppStatus,
    val iconUrl: String? = null
)


data class ServiceAccountCredential(
    @SerializedName("type") val type: String? = "service_account",
    @SerializedName("project_id") val projectId: String? = null,
    @SerializedName("private_key_id") val privateKeyId: String? = null,
    @SerializedName("private_key") val privateKeyPem: String? = null,
    @SerializedName("client_email") val clientEmail: String? = null,
    @SerializedName("client_id") val clientId: String? = null,
    @SerializedName("auth_uri") val authUri: String? = "https://accounts.google.com/o/oauth2/auth",
    @SerializedName("token_uri") val tokenUri: String? = "https://oauth2.googleapis.com/token"
)

data class StoreCatalogDto(
    val version: Int = 1,
    val stores: List<StoreJsonDto> = emptyList()
)

data class StoreJsonDto(
    val storeId: String,
    val storeName: String,
    val country: String? = "VN",
    val serviceAccountEmail: String? = null,
    val serviceAccountKey: ServiceAccountCredential? = null,
    val apps: List<AppJsonDto> = emptyList()
)

data class AppJsonDto(
    val packageName: String,
    val displayName: String,
    val trackNames: List<String> = listOf("production")
)
