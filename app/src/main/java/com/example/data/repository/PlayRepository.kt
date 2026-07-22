package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.*
import com.example.data.local.db.AppDatabase
import com.example.data.local.entity.*
import com.example.data.local.secure.SecureCredentialStore
import com.example.data.remote.androidpublisher.AndroidPublisherApiService
import com.example.data.remote.auth.AccessTokenManager
import com.example.domain.model.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient

class PlayRepository(
    private val db: AppDatabase,
    private val secureCredentialStore: SecureCredentialStore,
    private val accessTokenManager: AccessTokenManager,
    private val apiService: AndroidPublisherApiService
) {
    private val storeDao = db.storeDao()
    private val appDao = db.appDao()
    private val trackReleaseDao = db.trackReleaseDao()
    private val alertDao = db.alertDao()
    private val snapshotDao = db.snapshotDao()
    private val gson = Gson()

    // STORE FLOWS
    /**
     * Flow of all stores, mapping raw entities to domain Store model
     * Computes app counts, alerts, and live app stats per store.
     */
    fun getAllStoresFlow(): Flow<List<Store>> {
        return combine(storeDao.getAllStoresFlow(), appDao.getAllAppsFlow(), alertDao.getAllAlertsFlow()) { stores, apps, alerts ->
            stores.map { storeEntity ->
                val storeApps = apps.filter { it.storeId == storeEntity.id }
                val storeAlerts = alerts.filter { it.storeId == storeEntity.id && !it.isRead }
                val liveCount = storeApps.count { it.status == AppStatus.LIVE.name }
                Store(
                    id = storeEntity.id,
                    name = storeEntity.name,
                    country = storeEntity.country,
                    serviceAccountEmail = storeEntity.serviceAccountEmail,
                    hasCredential = secureCredentialStore.hasCredential(storeEntity.id),
                    connectionStatus = try { StoreConnectionStatus.valueOf(storeEntity.connectionStatus) } catch (e: Exception) { StoreConnectionStatus.CONNECTED },
                    appCount = storeApps.size,
                    releaseCount = storeApps.sumOf { it.latestVersionCode ?: 1 },
                    liveCount = liveCount,
                    alertCount = storeAlerts.size,
                    lastSyncAt = storeEntity.lastSyncAt,
                    autoSyncEnabled = storeEntity.autoSyncEnabled,
                    syncIntervalMinutes = storeEntity.syncIntervalMinutes
                )
            }
        }
    }

    /**
     * Fetches a specific store by ID with aggregated details.
     */
    suspend fun getStoreById(id: String): Store? {
        val storeEntity = storeDao.getStoreById(id) ?: return null
        val apps = appDao.getAppsByStore(id)
        val alerts = alertDao.getAllAlerts().filter { it.storeId == id && !it.isRead }
        val liveCount = apps.count { it.status == AppStatus.LIVE.name }
        return Store(
            id = storeEntity.id,
            name = storeEntity.name,
            country = storeEntity.country,
            serviceAccountEmail = storeEntity.serviceAccountEmail,
            hasCredential = secureCredentialStore.hasCredential(storeEntity.id),
            connectionStatus = try { StoreConnectionStatus.valueOf(storeEntity.connectionStatus) } catch (e: Exception) { StoreConnectionStatus.CONNECTED },
            appCount = apps.size,
            releaseCount = apps.sumOf { it.latestVersionCode ?: 1 },
            liveCount = liveCount,
            alertCount = alerts.size,
            lastSyncAt = storeEntity.lastSyncAt,
            autoSyncEnabled = storeEntity.autoSyncEnabled,
            syncIntervalMinutes = storeEntity.syncIntervalMinutes
        )
    }

    /**
     * Updates or inserts a store entity.
     */
    suspend fun saveStore(store: StoreEntity) {
        storeDao.upsertStore(store)
    }

    /**
     * Completely removes a store, including credentials and related apps.
     */
    suspend fun deleteStore(storeId: String) {
        secureCredentialStore.deleteCredential(storeId)
        accessTokenManager.invalidate(storeId)
        appDao.deleteByStoreId(storeId)
        storeDao.deleteStore(storeId)
    }

    suspend fun wipeAllCredentials() {
        secureCredentialStore.wipeAllCredentials()
    }

    // APP FLOWS
    /**
     * Flow of all apps across all stores, mapped to domain AppItem.
     */
    fun getAllAppsFlow(): Flow<List<AppItem>> {
        return combine(appDao.getAllAppsFlow(), storeDao.getAllStoresFlow(), alertDao.getAllAlertsFlow()) { apps, stores, alerts ->
            val storeMap = stores.associateBy { it.id }
            val manualRejectedAlerts = alerts.filter { it.isManual && it.type == AlertType.REJECTED.name && !it.isRead }
            apps.map { app ->
                val store = storeMap[app.storeId]
                val rejectedAlert = manualRejectedAlerts.find { it.appId == app.id || it.title.contains(app.displayName, true) }
                AppItem(
                    packageName = app.packageName,
                    storeId = app.storeId,
                    storeName = store?.name ?: "Store",
                    displayName = app.displayName,
                    mainTrack = app.defaultTrackNames ?: "Production",
                    status = if (rejectedAlert != null) AppStatus.REJECTED else try { AppStatus.valueOf(app.status) } catch (e: Exception) { AppStatus.LIVE },
                    latestVersionName = app.latestVersionName,
                    latestVersionCode = app.latestVersionCode,
                    userFraction = app.userFraction,
                    lastUpdatedTime = app.lastUpdatedTime,
                    hasManualRejectedAlert = rejectedAlert != null,
                    rejectedReason = rejectedAlert?.message
                )
            }
        }
    }

    fun getAppsByStoreFlow(storeId: String): Flow<List<AppItem>> {
        return getAllAppsFlow().map { apps -> apps.filter { it.storeId == storeId } }
    }

    /**
     * Flow of detailed application info (app data + its track releases).
     */
    fun getAppDetailFlow(packageName: String): Flow<AppDetail?> {
        return combine(appDao.getAppByPackageFlow(packageName), trackReleaseDao.getReleasesByPackageFlow(packageName), storeDao.getAllStoresFlow(), alertDao.getAllAlertsFlow()) { appEntity, releases, stores, alerts ->
            if (appEntity == null) return@combine null
            val store = stores.find { it.id == appEntity.storeId }
            val rejectedAlert = alerts.find { it.isManual && it.type == AlertType.REJECTED.name && (it.appId == packageName || it.title.contains(appEntity.displayName, true)) && !it.isRead }

            val appItem = AppItem(
                packageName = appEntity.packageName,
                storeId = appEntity.storeId,
                storeName = store?.name ?: "Store",
                displayName = appEntity.displayName,
                mainTrack = appEntity.defaultTrackNames ?: "Production",
                status = if (rejectedAlert != null) AppStatus.REJECTED else try { AppStatus.valueOf(appEntity.status) } catch (e: Exception) { AppStatus.LIVE },
                latestVersionName = appEntity.latestVersionName,
                latestVersionCode = appEntity.latestVersionCode,
                userFraction = appEntity.userFraction,
                lastUpdatedTime = appEntity.lastUpdatedTime,
                hasManualRejectedAlert = rejectedAlert != null,
                rejectedReason = rejectedAlert?.message
            )

            val trackList = releases.map { rel ->
                TrackRelease(
                    id = rel.id,
                    packageName = rel.packageName,
                    trackName = rel.trackName,
                    versionCode = rel.versionCode,
                    versionName = rel.versionName,
                    releaseStatus = rel.releaseStatus,
                    userFraction = rel.userFraction,
                    updatedTime = rel.updatedTime,
                    releaseNotes = rel.releaseNotes
                )
            }

            AppDetail(app = appItem, tracks = trackList, storeName = store?.name ?: "")
        }
    }

    // ALERTS FLOW
    fun getAllAlertsFlow(): Flow<List<AppAlert>> {
        return combine(alertDao.getAllAlertsFlow(), storeDao.getAllStoresFlow()) { alerts, stores ->
            val storeMap = stores.associateBy { it.id }
            alerts.map { a ->
                AppAlert(
                    id = a.id,
                    storeId = a.storeId,
                    storeName = storeMap[a.storeId]?.name ?: "Store",
                    appId = a.appId,
                    appName = a.title,
                    title = a.title,
                    message = a.message,
                    type = try { AlertType.valueOf(a.type) } catch (e: Exception) { AlertType.INFO },
                    isRead = a.isRead,
                    createdAt = a.createdAt,
                    isManual = a.isManual
                )
            }
        }
    }

    suspend fun insertManualAlert(title: String, message: String, type: AlertType, storeId: String, appId: String?) {
        val alert = GeneratedAlertEntity(
            id = "manual_${System.currentTimeMillis()}",
            storeId = storeId,
            appId = appId,
            title = title,
            message = message,
            type = type.name,
            isRead = false,
            createdAt = System.currentTimeMillis(),
            isManual = true
        )
        alertDao.insertAlert(alert)
    }

    suspend fun markAlertRead(id: String) {
        alertDao.markAlertRead(id)
    }

    suspend fun deleteAlert(id: String) {
        alertDao.deleteAlert(id)
    }

    // DASHBOARD & STATS
    /**
     * Flow of dashboard summary containing aggregated numbers and recent activities.
     */
    fun getDashboardSummaryFlow(): Flow<DashboardSummary> {
        return combine(storeDao.getAllStoresFlow(), appDao.getAllAppsFlow(), alertDao.getAllAlertsFlow()) { stores, apps, alerts ->
            val totalStores = stores.size
            val totalApps = apps.size
            val liveApps = apps.count { it.status == AppStatus.LIVE.name }
            val inReviewApps = apps.count { it.status == AppStatus.IN_REVIEW.name }
            val draftApps = apps.count { it.status == AppStatus.DRAFT.name }
            val actionRequiredApps = apps.count { it.status == AppStatus.ACTION_REQUIRED.name }
            val closedApps = apps.count { it.status == AppStatus.CLOSED.name }

            val manualRejectedCount = alerts.filter { it.isManual && it.type == AlertType.REJECTED.name && !it.isRead }.mapNotNull { it.appId }.distinct().size

            val totalReleases = apps.sumOf { it.latestVersionCode ?: 1 }
            val unreadAlerts = alerts.count { !it.isRead }

            val recentActivities = listOf(
                RecentActivity("1", "Camera Pro", "com.company.camera", "Đã phát hành lên Production", System.currentTimeMillis() - 120000, AppStatus.LIVE),
                RecentActivity("2", "AI Chat", "com.company.chat", "Đang trong quá trình review", System.currentTimeMillis() - 900000, AppStatus.IN_REVIEW),
                RecentActivity("3", "Photo Editor", "com.company.photo", "Bản nháp đã được tạo", System.currentTimeMillis() - 86400000, AppStatus.DRAFT)
            )

            DashboardSummary(
                totalStores = totalStores,
                storeIncrease = 2,
                totalApps = totalApps,
                appIncrease = 6,
                totalReleases = totalReleases,
                releaseIncrease = 2,
                alertCount = unreadAlerts,
                alertIncrease = 1,
                liveCount = liveApps,
                inReviewCount = inReviewApps,
                draftCount = draftApps,
                rejectedCount = manualRejectedCount,
                actionRequiredCount = actionRequiredApps,
                closedCount = closedApps,
                recentActivities = recentActivities
            )
        }
    }

    fun getDailySnapshotsFlow(): Flow<List<DailySnapshotEntity>> {
        return snapshotDao.getDailySnapshotsFlow()
    }

    // IMPORT CATALOG JSON (SECTION 10)
    /**
     * Parses and imports catalog JSON into local Room database.
     */
    suspend fun importCatalogJson(rawJson: String): String {
        Log.d("REPO", "Starting catalog import from JSON")
        return try {
            val catalog = gson.fromJson(rawJson, StoreCatalogDto::class.java)
            val storeList: List<StoreJsonDto> = catalog.stores ?: emptyList()
            if (storeList.isEmpty()) {
                Log.w("REPO", "No stores found in JSON")
                return "File JSON không có store nào."
            }

            for (storeDto: StoreJsonDto in storeList) {
                val existingStore = storeDao.getStoreById(storeDto.storeId)
                val storeEntity = StoreEntity(
                    id = storeDto.storeId,
                    name = storeDto.storeName,
                    country = storeDto.country ?: "VN",
                    serviceAccountEmail = storeDto.serviceAccountEmail,
                    hasCredential = secureCredentialStore.hasCredential(storeDto.storeId),
                    connectionStatus = existingStore?.connectionStatus ?: StoreConnectionStatus.CONNECTED.name,
                    lastSyncAt = System.currentTimeMillis(),
                    autoSyncEnabled = true,
                    syncIntervalMinutes = 15
                )
                storeDao.upsertStore(storeEntity)

                val appList: List<AppJsonDto> = storeDto.apps ?: emptyList()
                val appEntities = appList.map { appDto: AppJsonDto ->
                    AppEntity(
                        id = appDto.packageName,
                        storeId = storeDto.storeId,
                        packageName = appDto.packageName,
                        displayName = appDto.displayName,
                        defaultTrackNames = appDto.trackNames.firstOrNull() ?: "production",
                        status = AppStatus.LIVE.name,
                        latestVersionName = "1.0.0",
                        latestVersionCode = 1,
                        userFraction = 1.0f,
                        lastUpdatedTime = System.currentTimeMillis()
                    )
                }
                if (appEntities.isNotEmpty()) {
                    appDao.upsertApps(appEntities)
                }
            }
            Log.d("REPO", "Catalog import successful: ${storeList.size} stores")
            "Đã nhập thành công ${storeList.size} store!"
        } catch (e: Exception) {
            Log.e("REPO", "Failed to import JSON: ${e.message}")
            "Lỗi định dạng JSON: ${e.message}"
        }
    }

    // LIVE VERIFY & SYNC GOOGLE API (SECTIONS 1, 3, 5)
    /**
     * Validates and securely stores Service Account Credentials for a specific store.
     */
    suspend fun verifyAndSaveCredential(storeId: String, storeName: String, credentialJson: String): Result<String> {
        Log.d("REPO_SYNC", "Verifying credentials for store: $storeId")
        val credential = try {
            gson.fromJson(credentialJson, ServiceAccountCredential::class.java)
        } catch (e: Exception) {
            Log.e("REPO_SYNC", "Invalid JSON credential format")
            return Result.failure(IllegalArgumentException("File JSON không hợp lệ"))
        }

        if (credential == null || credential.type != "service_account" || credential.privateKeyPem.isNullOrBlank() || credential.clientEmail.isNullOrBlank()) {
            Log.e("REPO_SYNC", "Missing service account fields")
            return Result.failure(IllegalArgumentException("File JSON thiếu thông tin Service Account key (private_key hoặc client_email)"))
        }

        return try {
            // Test fetch token
            val token = accessTokenManager.getAccessToken(storeId, credential)
            if (token.isNotBlank()) {
                Log.d("REPO_SYNC", "Token fetch successful for $storeId, saving credentials")
                secureCredentialStore.saveCredential(storeId, credentialJson)
                // Update store state in DB
                val store = storeDao.getStoreById(storeId)
                if (store != null) {
                    storeDao.upsertStore(store.copy(hasCredential = true, connectionStatus = StoreConnectionStatus.CONNECTED.name))
                } else {
                    storeDao.upsertStore(StoreEntity(
                        id = storeId,
                        name = storeName.ifBlank { "New Store" },
                        country = null,
                        serviceAccountEmail = credential.clientEmail,
                        hasCredential = true,
                        connectionStatus = StoreConnectionStatus.CONNECTED.name,
                        lastSyncAt = null,
                        autoSyncEnabled = false,
                        syncIntervalMinutes = 60
                    ))
                }
                Result.success("Xác thực và lưu Service Account Key thành công!")
            } else {
                Log.e("REPO_SYNC", "Access token is blank for $storeId")
                Result.failure(IllegalStateException("Không lấy được Access Token từ Google"))
            }
        } catch (e: Exception) {
            Log.e("REPO_SYNC", "Verification failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Syncs all apps within a store with the Google Play Developer API.
     * Iterates through apps, fetches latest tracks, updates versions.
     */
    suspend fun syncStoreWithGoogle(storeId: String): Result<String> {
        Log.d("REPO_SYNC", "Starting Google sync for store: $storeId")
        val credential = secureCredentialStore.getCredential(storeId)
            ?: run {
                Log.w("REPO_SYNC", "No credential found for store: $storeId")
                return Result.failure(IllegalStateException("Store chưa được cấu hình Service Account Key"))
            }

        return try {
            val token = accessTokenManager.getAccessToken(storeId, credential)
            val apps = appDao.getAppsByStore(storeId)
            Log.d("REPO_SYNC", "Syncing ${apps.size} apps for store: $storeId")

            var successCount = 0
            for (app in apps) {
                try {
                    Log.d("REPO_SYNC", "Syncing app: ${app.packageName}")
                    val tracks = apiService.fetchTracksWithEditLifecycle(token, app.packageName)
                    if (tracks.isNotEmpty()) {
                        val trackEntities = tracks.map { rel ->
                            TrackReleaseEntity(
                                id = "${rel.packageName}_${rel.trackName}_${rel.versionCode}",
                                packageName = rel.packageName,
                                trackName = rel.trackName,
                                versionCode = rel.versionCode,
                                versionName = rel.versionName,
                                releaseStatus = rel.releaseStatus,
                                userFraction = rel.userFraction,
                                updatedTime = rel.updatedTime,
                                releaseNotes = rel.releaseNotes
                            )
                        }
                        trackReleaseDao.upsertReleases(trackEntities)

                        val latest = tracks.maxByOrNull { it.versionCode }
                        if (latest != null) {
                            appDao.upsertApp(
                                app.copy(
                                    latestVersionName = latest.versionName,
                                    latestVersionCode = latest.versionCode,
                                    userFraction = latest.userFraction,
                                    lastUpdatedTime = System.currentTimeMillis()
                                )
                            )
                        }
                        successCount++
                    }
                } catch (e: Exception) {
                    Log.w("REPO_SYNC", "Failed to sync app ${app.packageName}: ${e.message}")
                    // Ignore single package sync error
                }
            }

            val store = storeDao.getStoreById(storeId)
            if (store != null) {
                storeDao.upsertStore(store.copy(lastSyncAt = System.currentTimeMillis(), connectionStatus = StoreConnectionStatus.CONNECTED.name))
            }

            Log.d("REPO_SYNC", "Successfully synced $successCount/${apps.size} apps for $storeId")
            Result.success("Đã đồng bộ $successCount/${apps.size} ứng dụng thành công.")
        } catch (e: Exception) {
            Log.e("REPO_SYNC", "Overall sync error for $storeId: ${e.message}")
            val store = storeDao.getStoreById(storeId)
            if (store != null) {
                storeDao.upsertStore(store.copy(connectionStatus = StoreConnectionStatus.CONNECTION_ERROR.name))
            }
            // Generate Connection Error alert
            alertDao.insertAlert(
                GeneratedAlertEntity(
                    id = "err_${System.currentTimeMillis()}",
                    storeId = storeId,
                    appId = null,
                    title = "Lỗi kết nối đồng bộ",
                    message = "Không thể đồng bộ với Google API: ${e.message}",
                    type = AlertType.CONNECTION_ERROR.name,
                    isRead = false,
                    createdAt = System.currentTimeMillis(),
                    isManual = false
                )
            )
            Result.failure(e)
        }
    }
}
