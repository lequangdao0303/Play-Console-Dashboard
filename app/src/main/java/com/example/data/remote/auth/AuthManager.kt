package com.example.data.remote.auth

import android.util.Base64
import com.example.domain.model.ServiceAccountCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.concurrent.ConcurrentHashMap

class ServiceAccountJwtSigner {

    fun signAssertion(credential: ServiceAccountCredential): String {
        val clientEmail = credential.clientEmail ?: throw IllegalArgumentException("client_email missing")
        val tokenUri = credential.tokenUri ?: "https://oauth2.googleapis.com/token"
        val privateKeyPem = credential.privateKeyPem ?: throw IllegalArgumentException("private_key missing")

        val header = """{"alg":"RS256","typ":"JWT"}"""
        val now = System.currentTimeMillis() / 1000
        val exp = now + 3600
        val claims = """
            {
              "iss": "$clientEmail",
              "scope": "https://www.googleapis.com/auth/androidpublisher",
              "aud": "$tokenUri",
              "exp": $exp,
              "iat": $now
            }
        """.trimIndent()

        val encodedHeader = base64UrlEncode(header.toByteArray(Charsets.UTF_8))
        val encodedClaims = base64UrlEncode(claims.toByteArray(Charsets.UTF_8))
        val signingInput = "$encodedHeader.$encodedClaims"

        val privateKey = parsePkcs8PrivateKey(privateKeyPem)
        val signature = Signature.getInstance("SHA256withRSA").apply {
            initSign(privateKey)
            update(signingInput.toByteArray(Charsets.UTF_8))
        }.sign()

        val encodedSignature = base64UrlEncode(signature)
        return "$signingInput.$encodedSignature"
    }

    private fun parsePkcs8PrivateKey(pem: String): PrivateKey {
        val clean = pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.decode(clean, Base64.DEFAULT)
        val spec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(spec)
    }

    private fun base64UrlEncode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}

class AccessTokenManager(
    private val httpClient: OkHttpClient,
    private val signer: ServiceAccountJwtSigner
) {
    private val mutex = Mutex()
    private val tokenCache = ConcurrentHashMap<String, CachedToken>()

    private data class CachedToken(val token: String, val expiresAt: Long)

    suspend fun getAccessToken(storeId: String, credential: ServiceAccountCredential): String = mutex.withLock {
        val now = System.currentTimeMillis()
        val cached = tokenCache[storeId]
        if (cached != null && cached.expiresAt > now + 300_000) { // refresh if less than 5 minutes left
            return@withLock cached.token
        }

        return withContext(Dispatchers.IO) {
            val jwt = signer.signAssertion(credential)
            val tokenUri = credential.tokenUri ?: "https://oauth2.googleapis.com/token"

            val requestBody = FormBody.Builder()
                .add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                .add("assertion", jwt)
                .build()

            val request = Request.Builder()
                .url(tokenUri)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                throw IllegalStateException("Lỗi xác thực OAuth Google ($storeId): HTTP ${response.code} $responseBody")
            }

            val json = JSONObject(responseBody)
            val token = json.getString("access_token")
            val expiresInSeconds = json.optLong("expires_in", 3600)
            val expiresAt = now + (expiresInSeconds * 1000)

            tokenCache[storeId] = CachedToken(token, expiresAt)
            token
        }
    }

    fun invalidate(storeId: String) {
        tokenCache.remove(storeId)
    }
}
