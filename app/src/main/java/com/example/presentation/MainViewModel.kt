package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.db.AppDatabase
import com.example.data.local.db.InitialData
import com.example.data.local.secure.SecureCredentialStore
import com.example.data.remote.androidpublisher.AndroidPublisherApiService
import com.example.data.remote.auth.AccessTokenManager
import com.example.data.remote.auth.ServiceAccountJwtSigner
import com.example.data.repository.PlayRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val secureCredentialStore = SecureCredentialStore(application)
    private val httpClient = OkHttpClient.Builder().build()
    private val jwtSigner = ServiceAccountJwtSigner()
    private val accessTokenManager = AccessTokenManager(httpClient, jwtSigner)
    private val apiService = AndroidPublisherApiService(httpClient)

    val repository = PlayRepository(db, secureCredentialStore, accessTokenManager, apiService)

    // State flows
    val stores: StateFlow<List<Store>> = repository.getAllStoresFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val apps: StateFlow<List<AppItem>> = repository.getAllAppsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<AppAlert>> = repository.getAllAlertsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardSummary: StateFlow<DashboardSummary> = repository.getDashboardSummaryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    val dailySnapshots = repository.getDailySnapshotsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            InitialData.populateIfEmpty(
                db.storeDao(),
                db.appDao(),
                db.trackReleaseDao(),
                db.alertDao(),
                db.snapshotDao()
            )
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun syncStore(storeId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.syncStoreWithGoogle(storeId)
            _isRefreshing.value = false
            result.onSuccess {
                _userMessage.value = it
            }.onFailure {
                _userMessage.value = "Lỗi đồng bộ: ${it.message}"
            }
        }
    }

    fun verifyAndAddStoreCredential(storeId: String, credentialJson: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyAndSaveCredential(storeId, credentialJson)
            result.onSuccess { msg ->
                onResult(true, msg)
            }.onFailure { err ->
                onResult(false, err.message ?: "Xác thực không thành công")
            }
        }
    }

    fun importCatalog(rawJson: String) {
        viewModelScope.launch {
            val resultMsg = repository.importCatalogJson(rawJson)
            _userMessage.value = resultMsg
        }
    }

    fun addManualAlert(title: String, message: String, type: AlertType, storeId: String, appId: String?) {
        viewModelScope.launch {
            repository.insertManualAlert(title, message, type, storeId, appId)
            _userMessage.value = "Đã tạo cảnh báo thủ công thành công"
        }
    }

    fun markAlertRead(alertId: String) {
        viewModelScope.launch {
            repository.markAlertRead(alertId)
        }
    }

    fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            repository.deleteAlert(alertId)
        }
    }

    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            repository.deleteStore(storeId)
            _userMessage.value = "Đã xoá Store thành công"
        }
    }

    fun wipeAllCredentials() {
        viewModelScope.launch {
            repository.wipeAllCredentials()
            _userMessage.value = "Đã xoá toàn bộ credential cục bộ an toàn"
        }
    }
}
