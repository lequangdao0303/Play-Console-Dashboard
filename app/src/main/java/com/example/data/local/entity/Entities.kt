package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String?,
    val serviceAccountEmail: String?,
    val hasCredential: Boolean,
    val connectionStatus: String,
    val lastSyncAt: Long?,
    val autoSyncEnabled: Boolean,
    val syncIntervalMinutes: Int
)

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val id: String, // packageName
    val storeId: String,
    val packageName: String,
    val displayName: String,
    val defaultTrackNames: String?,
    val status: String,
    val latestVersionName: String?,
    val latestVersionCode: Int?,
    val userFraction: Float,
    val lastUpdatedTime: Long
)

@Entity(tableName = "track_releases")
data class TrackReleaseEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val trackName: String,
    val versionCode: Int,
    val versionName: String,
    val releaseStatus: String,
    val userFraction: Float,
    val updatedTime: Long,
    val releaseNotes: String?
)

@Entity(tableName = "local_release_snapshots")
data class LocalReleaseSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val storeId: String,
    val trackName: String,
    val versionCode: Int,
    val versionName: String,
    val status: String,
    val timestamp: Long
)

@Entity(tableName = "daily_snapshots")
data class DailySnapshotEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val totalApps: Int,
    val liveCount: Int,
    val inReviewCount: Int,
    val draftCount: Int,
    val actionRequiredCount: Int,
    val closedCount: Int,
    val timestamp: Long
)

@Entity(tableName = "generated_alerts")
data class GeneratedAlertEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val appId: String?,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: Long,
    val isManual: Boolean
)

@Entity(tableName = "api_quota_logs")
data class ApiQuotaLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: String,
    val date: String,
    val callCount: Int
)
