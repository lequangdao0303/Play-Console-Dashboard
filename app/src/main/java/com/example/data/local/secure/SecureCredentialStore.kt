package com.example.data.local.secure

import android.app.KeyguardManager
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.domain.model.ServiceAccountCredential
import com.google.gson.Gson

class SecureCredentialStore(private val context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_play_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val gson = Gson()

    fun isDeviceSecure(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return keyguardManager?.isDeviceSecure == true
    }

    fun saveCredential(storeId: String, jsonContent: String) {
        encryptedPrefs.edit().putString("cred_$storeId", jsonContent).apply()
    }

    fun getCredential(storeId: String): ServiceAccountCredential? {
        val json = encryptedPrefs.getString("cred_$storeId", null) ?: return null
        return try {
            gson.fromJson(json, ServiceAccountCredential::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getCredentialRawJson(storeId: String): String? {
        return encryptedPrefs.getString("cred_$storeId", null)
    }

    fun hasCredential(storeId: String): Boolean {
        return encryptedPrefs.contains("cred_$storeId")
    }

    fun deleteCredential(storeId: String) {
        encryptedPrefs.edit().remove("cred_$storeId").apply()
    }

    fun wipeAllCredentials() {
        encryptedPrefs.edit().clear().apply()
    }
}
