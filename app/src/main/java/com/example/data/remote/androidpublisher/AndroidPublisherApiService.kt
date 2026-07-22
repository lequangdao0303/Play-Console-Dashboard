package com.example.data.remote.androidpublisher

import android.util.Log
import com.example.domain.model.TrackRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AndroidPublisherApiService(
    private val httpClient: OkHttpClient
) {
    private val baseUrl = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications"

    /**
     * Creates a new edit session for the given app package.
     * This is required to read or modify app data via the Google Play Developer API.
     */
    suspend fun createEditSession(accessToken: String, packageName: String): String = withContext(Dispatchers.IO) {
        val url = "$baseUrl/$packageName/edits"
        Log.d("API_CALL", "Creating edit session for package: $packageName")
        val requestBody = ByteArray(0).toRequestBody(null)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        val response = httpClient.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e("API_CALL", "Error creating edit session: ${response.code} - $bodyStr")
            throw IllegalStateException("Google API Error (${response.code}): $bodyStr")
        }

        val json = JSONObject(bodyStr)
        val editId = json.getString("id")
        Log.d("API_CALL", "Edit session created successfully. EditId: $editId")
        editId
    }

    /**
     * Fetches track releases from a specific edit session.
     * Extracts version codes, release status, and release notes for each track.
     */
    suspend fun listTracks(accessToken: String, packageName: String, editId: String): List<TrackRelease> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/$packageName/edits/$editId/tracks"
        Log.d("API_CALL", "Fetching tracks for package: $packageName, EditId: $editId")
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        val response = httpClient.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e("API_CALL", "Error listing tracks: ${response.code} - $bodyStr")
            throw IllegalStateException("Google API List Tracks Error (${response.code}): $bodyStr")
        }

        val resultList = mutableListOf<TrackRelease>()
        val json = JSONObject(bodyStr)
        val tracksArray = json.optJSONArray("tracks") ?: return@withContext emptyList()

        for (i in 0 until tracksArray.length()) {
            val trackObj = tracksArray.getJSONObject(i)
            val trackName = trackObj.optString("track", "unknown")
            val releasesArray = trackObj.optJSONArray("releases") ?: continue

            for (j in 0 until releasesArray.length()) {
                val relObj = releasesArray.getJSONObject(j)
                val versionName = relObj.optString("name", "1.0.0")
                val versionCodes = relObj.optJSONArray("versionCodes")
                val versionCode = if (versionCodes != null && versionCodes.length() > 0) versionCodes.optInt(0, 1) else 1
                val releaseStatus = relObj.optString("status", "completed")
                val userFraction = relObj.optDouble("userFraction", 1.0).toFloat()

                var notesStr: String? = null
                val notesArray = relObj.optJSONArray("releaseNotes")
                if (notesArray != null && notesArray.length() > 0) {
                    val noteObj = notesArray.getJSONObject(0)
                    notesStr = noteObj.optString("text", null)
                }

                resultList.add(
                    TrackRelease(
                        id = "${packageName}_${trackName}_$versionCode",
                        packageName = packageName,
                        trackName = trackName.replaceFirstChar { it.uppercase() },
                        versionCode = versionCode,
                        versionName = versionName,
                        releaseStatus = releaseStatus,
                        userFraction = userFraction,
                        updatedTime = System.currentTimeMillis(),
                        releaseNotes = notesStr
                    )
                )
            }
        }
        Log.d("API_CALL", "Found ${resultList.size} releases across tracks for $packageName")
        resultList
    }

    /**
     * Discards the edit session to prevent leaving uncommitted changes.
     */
    suspend fun deleteEditSession(accessToken: String, packageName: String, editId: String) = withContext(Dispatchers.IO) {
        val url = "$baseUrl/$packageName/edits/$editId"
        Log.d("API_CALL", "Deleting edit session for package: $packageName, EditId: $editId")
        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d("API_CALL", "Edit session deleted successfully.")
            } else {
                Log.w("API_CALL", "Failed to delete edit session: ${response.code}")
            }
            response.close()
        } catch (e: Exception) {
            Log.e("API_CALL", "Exception while deleting edit session: ${e.message}")
        }
    }

    /**
     * Safely executes the edit session lifecycle: insert -> listTracks -> delete
     * This ensures the edit session is always cleaned up after fetching tracks.
     */
    suspend fun fetchTracksWithEditLifecycle(
        accessToken: String,
        packageName: String
    ): List<TrackRelease> {
        var editId: String? = null
        return try {
            Log.d("API_CALL", "Starting track fetch lifecycle for $packageName")
            editId = createEditSession(accessToken, packageName)
            listTracks(accessToken, packageName, editId)
        } finally {
            editId?.let { id ->
                deleteEditSession(accessToken, packageName, id)
            }
        }
    }

    /**
     * Fetches the app icon URL from the Google Play Developer API
     */
    suspend fun fetchAppIconUrl(accessToken: String, packageName: String, editId: String): String? = withContext(Dispatchers.IO) {
        val listingsUrl = "$baseUrl/$packageName/edits/$editId/listings"
        var language = "en-US"
        try {
            val listReq = Request.Builder().url(listingsUrl).get().addHeader("Authorization", "Bearer $accessToken").build()
            val listRes = httpClient.newCall(listReq).execute()
            if (listRes.isSuccessful) {
                val listBody = listRes.body?.string() ?: ""
                val json = JSONObject(listBody)
                val listingsArray = json.optJSONArray("listings")
                if (listingsArray != null && listingsArray.length() > 0) {
                    val firstListing = listingsArray.getJSONObject(0)
                    language = firstListing.optString("language", "en-US")
                }
            }
            listRes.close()
        } catch (e: Exception) {
            Log.e("API_CALL", "Error fetching listings to get language: ${e.message}")
        }

        val imagesUrl = "$baseUrl/$packageName/edits/$editId/listings/$language/icon"
        val request = Request.Builder()
            .url(imagesUrl)
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        try {
            val response = httpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(bodyStr)
                val imagesArray = json.optJSONArray("images")
                if (imagesArray != null && imagesArray.length() > 0) {
                    val firstImage = imagesArray.getJSONObject(0)
                    return@withContext firstImage.optString("url", null)
                }
            } else {
                Log.w("API_CALL", "Failed to fetch icon: ${response.code} $bodyStr")
            }
            response.close()
        } catch (e: Exception) {
            Log.e("API_CALL", "Exception fetching icon: ${e.message}")
        }
        null
    }
}
