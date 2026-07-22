package com.example.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Tổng quan")
    object Stores : Screen("stores", "Danh sách Stores")
    object StoreDetail : Screen("store_detail/{storeId}", "Chi tiết Store") {
        fun createRoute(storeId: String) = "store_detail/$storeId"
    }
    object Apps : Screen("apps", "Ứng dụng")
    object AppDetail : Screen("app_detail/{packageName}", "Chi tiết Ứng dụng") {
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
    object ReleaseDetail : Screen("release_detail/{packageName}/{trackName}/{versionCode}", "Chi tiết Phiên bản") {
        fun createRoute(packageName: String, trackName: String, versionCode: Int) = "release_detail/$packageName/$trackName/$versionCode"
    }
    object AddStore : Screen("add_store", "Thêm Store")
    object StoreSettings : Screen("store_settings/{storeId}", "Cài đặt Store") {
        fun createRoute(storeId: String) = "store_settings/$storeId"
    }
    object ReleaseHistory : Screen("release_history", "Lịch sử phát hành")
    object Alerts : Screen("alerts", "Cảnh báo")
    object Statistics : Screen("statistics", "Thống kê")
    object Settings : Screen("settings", "Cài đặt")
}
