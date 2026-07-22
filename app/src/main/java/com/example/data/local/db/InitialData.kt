package com.example.data.local.db

import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.domain.model.AppStatus
import com.example.domain.model.StoreConnectionStatus

object InitialData {

    suspend fun populateIfEmpty(
        storeDao: StoreDao,
        appDao: AppDao,
        trackReleaseDao: TrackReleaseDao,
        alertDao: AlertDao,
        snapshotDao: SnapshotDao
    ) {
        val existingStores = storeDao.getAllStores()
        if (existingStores.isNotEmpty()) return

        // 1. Prepopulate Stores
        val stores = listOf(
            StoreEntity("store_vn", "VN Store", "VN", "vnstore@company.com", true, StoreConnectionStatus.CONNECTED.name, System.currentTimeMillis() - 120000, true, 15),
            StoreEntity("store_us", "US Store", "US", "usstore@company.com", true, StoreConnectionStatus.CONNECTED.name, System.currentTimeMillis() - 300000, true, 15),
            StoreEntity("store_jp", "JP Store", "JP", "jpstore@company.com", true, StoreConnectionStatus.CONNECTED.name, System.currentTimeMillis() - 420000, true, 15),
            StoreEntity("store_eu", "EU Store", "EU", "eustore@company.com", false, StoreConnectionStatus.CONNECTION_ERROR.name, System.currentTimeMillis() - 3600000, false, 60),
            StoreEntity("store_kr", "KR Store", "KR", "krstore@company.com", true, StoreConnectionStatus.CONNECTED.name, System.currentTimeMillis() - 180000, true, 15)
        )
        storeDao.upsertStores(stores)

        // 2. Prepopulate Apps
        val now = System.currentTimeMillis()
        val apps = listOf(
            AppEntity("com.company.camera", "store_vn", "com.company.camera", "Camera Pro", "production", AppStatus.LIVE.name, "2.1.0", 120, 1.0f, now - 7200000),
            AppEntity("com.company.chat", "store_vn", "com.company.chat", "AI Chat", "open_testing", AppStatus.IN_REVIEW.name, "3.0.1", 31, 0.5f, now - 900000),
            AppEntity("com.company.photo", "store_vn", "com.company.photo", "Photo Editor", "production", AppStatus.LIVE.name, "1.5.4", 54, 1.0f, now - 86400000),
            AppEntity("com.company.video", "store_vn", "com.company.video", "Video Maker", "internal_testing", AppStatus.DRAFT.name, "1.0.3", 15, 0.1f, now - 172800000),
            AppEntity("com.company.pdf", "store_vn", "com.company.pdf", "PDF Reader", "production", AppStatus.REJECTED.name, "2.0.0", 22, 1.0f, now - 259200000),
            AppEntity("com.company.notes", "store_vn", "com.company.notes", "Notes Plus", "production", AppStatus.LIVE.name, "1.2.0", 12, 1.0f, now - 432000000),

            // US Store apps
            AppEntity("com.company.us.scanner", "store_us", "com.company.us.scanner", "Doc Scanner US", "production", AppStatus.LIVE.name, "4.1.0", 410, 1.0f, now - 100000),
            AppEntity("com.company.us.fitness", "store_us", "com.company.us.fitness", "Fitness Tracker", "production", AppStatus.LIVE.name, "2.0.1", 201, 1.0f, now - 200000),

            // EU Store apps
            AppEntity("com.company.eu.wallet", "store_eu", "com.company.eu.wallet", "Crypto Wallet EU", "production", AppStatus.ACTION_REQUIRED.name, "1.1.0", 110, 0.8f, now - 500000)
        )
        appDao.upsertApps(apps)

        // 3. Prepopulate Releases for Camera Pro and others
        val releases = listOf(
            TrackReleaseEntity("com.company.camera_prod_120", "com.company.camera", "Production", 120, "2.1.0", "completed", 1.0f, now - 7200000, "Cải thiện hiệu suất ứng dụng\nSửa lỗi crash khi quay video\nHỗ trợ thêm định dạng HEIC"),
            TrackReleaseEntity("com.company.camera_prod_118", "com.company.camera", "Production", 118, "2.0.9", "completed", 1.0f, now - 864000000, "Sửa lỗi giao diện camera"),
            TrackReleaseEntity("com.company.camera_prod_116", "com.company.camera", "Production", 116, "2.0.8", "completed", 1.0f, now - 2592000000, "Nâng cấp bộ lọc màu"),
            TrackReleaseEntity("com.company.camera_beta_121", "com.company.camera", "Open Testing", 121, "2.1.0-beta.1", "inProgress", 0.2f, now - 3600000, "Thử nghiệm tính năng AI Retouch"),

            TrackReleaseEntity("com.company.chat_open_31", "com.company.chat", "Open Testing", 31, "3.0.1", "inProgress", 0.5f, now - 900000, "Cập nhật mô hình AI Gemini mới"),
            TrackReleaseEntity("com.company.photo_prod_54", "com.company.photo", "Production", 54, "1.5.4", "completed", 1.0f, now - 86400000, "Thêm 15 khung hình mới"),
            TrackReleaseEntity("com.company.pdf_prod_22", "com.company.pdf", "Production", 22, "2.0.0", "halted", 1.0f, now - 259200000, "Tối ưu dung lượng file PDF")
        )
        trackReleaseDao.upsertReleases(releases)

        // 4. Prepopulate Alerts (matching mockup Screen 10)
        val alerts = listOf(
            GeneratedAlertEntity(
                id = "alert_1",
                storeId = "store_eu",
                appId = null,
                title = "Lỗi kết nối với EU Store",
                message = "Không thể đồng bộ dữ liệu. Vui lòng kiểm tra lại file JSON hoặc quyền truy cập.",
                type = "CONNECTION_ERROR",
                isRead = false,
                createdAt = now - 120000,
                isManual = false
            ),
            GeneratedAlertEntity(
                id = "alert_2",
                storeId = "store_vn",
                appId = "com.company.pdf",
                title = "Ứng dụng bị từ chối",
                message = "Photo Editor v2.9.0 bị từ chối trên Production.\nLý do: Vi phạm chính sách về quyền riêng tư.",
                type = "REJECTED",
                isRead = false,
                createdAt = System.currentTimeMillis() - 3600000,
                isManual = true
            ),
            GeneratedAlertEntity(
                id = "alert_3",
                storeId = "store_eu",
                appId = "com.company.eu.wallet",
                title = "Yêu cầu hành động",
                message = "Photo Editor cần cập nhật thông tin nội dung. Vui lòng kiểm tra trong Play Console.",
                type = "ACTION_REQUIRED",
                isRead = false,
                createdAt = System.currentTimeMillis() - 18000000,
                isManual = false
            ),
            GeneratedAlertEntity(
                id = "alert_4",
                storeId = "store_vn",
                appId = null,
                title = "Đồng bộ hoàn tất",
                message = "VN Store đã đồng bộ thành công. 18 ứng dụng được cập nhật.",
                type = "INFO",
                isRead = true,
                createdAt = System.currentTimeMillis() - 7200000,
                isManual = false
            )
        )
        for (alert in alerts) {
            alertDao.insertAlert(alert)
        }

        // 5. Prepopulate Daily Snapshots for 8 days trend chart (Screen 11)
        val snapshots = listOf(
            DailySnapshotEntity("14/06", 58, 20, 8, 8, 2, 5, now - 604800000),
            DailySnapshotEntity("15/06", 58, 21, 9, 7, 2, 5, now - 518400000),
            DailySnapshotEntity("16/06", 58, 22, 10, 6, 2, 5, now - 432000000),
            DailySnapshotEntity("17/06", 58, 23, 9, 6, 3, 5, now - 345600000),
            DailySnapshotEntity("18/06", 58, 24, 10, 7, 3, 5, now - 259200000),
            DailySnapshotEntity("19/06", 58, 24, 11, 7, 3, 5, now - 172800000),
            DailySnapshotEntity("20/06", 58, 25, 10, 8, 3, 5, now - 86400000),
            DailySnapshotEntity("21/06", 58, 25, 10, 8, 3, 7, now)
        )
        for (s in snapshots) {
            snapshotDao.insertDailySnapshot(s)
        }
    }
}
