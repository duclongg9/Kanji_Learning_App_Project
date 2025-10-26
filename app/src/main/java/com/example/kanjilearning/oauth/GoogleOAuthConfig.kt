package com.example.kanjilearning.oauth

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.annotation.RawRes
import org.json.JSONObject

/**
 * Simple holder for the Google OAuth configuration used by Google Sign-In.
 */
data class GoogleOAuthConfig(val clientId: String) {

    companion object {
        private const val TAG = "GoogleOAuthConfig"

        fun fromRawResource(
            context: Context,
            @RawRes resId: Int
        ): GoogleOAuthConfig? {
            val content = try {
                context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
            } catch (error: Resources.NotFoundException) {
                Log.w(TAG, "OAuth config resource not found", error)
                return null
            } catch (error: Exception) {
                Log.w(TAG, "Failed to read OAuth config", error)
                return null
            }

            if (content.isBlank()) {
                Log.w(TAG, "OAuth config file is empty")
                return null
            }

            return try {
                val json = JSONObject(content)
                val clientId = json.optJSONObject("installed")?.optString("client_id")?.trim()
                    .orEmpty()
                if (clientId.isBlank()) {
                    Log.w(TAG, "OAuth config does not contain a client_id")
                    null
                } else {
                    GoogleOAuthConfig(clientId)
                }
            } catch (error: Exception) {
                Log.w(TAG, "Failed to parse OAuth config", error)
                null
            }
        }
    }
}
