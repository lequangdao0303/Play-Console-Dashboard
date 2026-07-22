package com.example.data.local.db

import com.example.data.local.dao.*

object InitialData {
    suspend fun populateIfEmpty(
        storeDao: StoreDao,
        appDao: AppDao,
        trackReleaseDao: TrackReleaseDao,
        alertDao: AlertDao,
        snapshotDao: SnapshotDao
    ) {
        // No mock data insertion
    }
}
