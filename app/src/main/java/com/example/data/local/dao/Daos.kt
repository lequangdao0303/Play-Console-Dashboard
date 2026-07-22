package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY name ASC")
    fun getAllStoresFlow(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores ORDER BY name ASC")
    suspend fun getAllStores(): List<StoreEntity>

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getStoreById(id: String): StoreEntity?

    @Query("SELECT * FROM stores WHERE id = :id")
    fun getStoreByIdFlow(id: String): Flow<StoreEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStore(store: StoreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStores(stores: List<StoreEntity>)

    @Query("DELETE FROM stores WHERE id = :id")
    suspend fun deleteStore(id: String)
}

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY displayName ASC")
    fun getAllAppsFlow(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY displayName ASC")
    suspend fun getAllApps(): List<AppEntity>

    @Query("SELECT * FROM apps WHERE storeId = :storeId ORDER BY displayName ASC")
    fun getAppsByStoreFlow(storeId: String): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE storeId = :storeId ORDER BY displayName ASC")
    suspend fun getAppsByStore(storeId: String): List<AppEntity>

    @Query("SELECT * FROM apps WHERE id = :packageName")
    suspend fun getAppByPackage(packageName: String): AppEntity?

    @Query("SELECT * FROM apps WHERE id = :packageName")
    fun getAppByPackageFlow(packageName: String): Flow<AppEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApp(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApps(apps: List<AppEntity>)

    @Query("DELETE FROM apps WHERE storeId = :storeId")
    suspend fun deleteByStoreId(storeId: String)

    @Query("DELETE FROM apps WHERE id = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}

@Dao
interface TrackReleaseDao {
    @Query("SELECT * FROM track_releases WHERE packageName = :packageName ORDER BY versionCode DESC")
    fun getReleasesByPackageFlow(packageName: String): Flow<List<TrackReleaseEntity>>

    @Query("SELECT * FROM track_releases WHERE packageName = :packageName ORDER BY versionCode DESC")
    suspend fun getReleasesByPackage(packageName: String): List<TrackReleaseEntity>

    @Query("SELECT * FROM track_releases ORDER BY updatedTime DESC")
    fun getAllReleasesFlow(): Flow<List<TrackReleaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReleases(releases: List<TrackReleaseEntity>)

    @Query("DELETE FROM track_releases WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM generated_alerts ORDER BY createdAt DESC")
    fun getAllAlertsFlow(): Flow<List<GeneratedAlertEntity>>

    @Query("SELECT * FROM generated_alerts ORDER BY createdAt DESC")
    suspend fun getAllAlerts(): List<GeneratedAlertEntity>

    @Query("SELECT * FROM generated_alerts WHERE isRead = 0")
    fun getUnreadAlertsFlow(): Flow<List<GeneratedAlertEntity>>

    @Query("SELECT * FROM generated_alerts WHERE type = :type AND isManual = 1")
    suspend fun getActiveManualAlertsByType(type: String): List<GeneratedAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: GeneratedAlertEntity)

    @Query("UPDATE generated_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAlertRead(id: String)

    @Query("DELETE FROM generated_alerts WHERE id = :id")
    suspend fun deleteAlert(id: String)
}

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM daily_snapshots ORDER BY date ASC")
    fun getDailySnapshotsFlow(): Flow<List<DailySnapshotEntity>>

    @Query("SELECT * FROM daily_snapshots ORDER BY date ASC")
    suspend fun getDailySnapshots(): List<DailySnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySnapshot(snapshot: DailySnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReleaseSnapshot(snapshot: LocalReleaseSnapshotEntity)
}
