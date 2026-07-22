package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.db.AppDatabase
import com.example.data.local.db.InitialData
import com.example.domain.model.AppStatus
import com.example.domain.model.ServiceAccountCredential
import com.example.domain.model.StoreConnectionStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlayConsoleDashboardTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testSeedDataInsertionAndStoreQueries() = runBlocking {
        InitialData.populateIfEmpty(
            db.storeDao(),
            db.appDao(),
            db.trackReleaseDao(),
            db.alertDao(),
            db.snapshotDao()
        )

        val loadedStore = db.storeDao().getStoreById("store_vn")
        assertNotNull(loadedStore)
        assertEquals("VN Store", loadedStore?.name)
        assertEquals("VN", loadedStore?.country)

        val loadedApps = db.appDao().getAllApps()
        assertTrue(loadedApps.isNotEmpty())
        val cameraApp = loadedApps.find { it.packageName == "com.company.camera" }
        assertNotNull(cameraApp)
        assertEquals("Camera Pro", cameraApp?.displayName)
    }

    @Test
    fun testServiceAccountCredentialValidation() {
        val invalidCred = ServiceAccountCredential(
            type = "service_account",
            clientEmail = null,
            privateKeyPem = null
        )
        assertTrue(invalidCred.clientEmail.isNullOrBlank())
        assertTrue(invalidCred.privateKeyPem.isNullOrBlank())

        val validCred = ServiceAccountCredential(
            type = "service_account",
            clientEmail = "test@project.iam.gserviceaccount.com",
            privateKeyPem = "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC...\n-----END PRIVATE KEY-----"
        )
        assertEquals("service_account", validCred.type)
        assertEquals("test@project.iam.gserviceaccount.com", validCred.clientEmail)
        assertNotNull(validCred.privateKeyPem)
    }

    @Test
    fun testAppStatusEnumValues() {
        assertEquals("Live", AppStatus.LIVE.displayName)
        assertEquals("In Review", AppStatus.IN_REVIEW.displayName)
        assertEquals("Draft", AppStatus.DRAFT.displayName)
        assertEquals("Rejected", AppStatus.REJECTED.displayName)
        assertEquals("Kết nối", StoreConnectionStatus.CONNECTED.label)
    }
}
