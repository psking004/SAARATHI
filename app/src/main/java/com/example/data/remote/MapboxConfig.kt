package com.example.data.remote

import com.example.BuildConfig

/**
 * Clean Mapbox configuration manager.
 * Safely accesses MAPBOX_ACCESS_TOKEN from BuildConfig (.env / .env.example).
 * Never hardcodes tokens.
 * Provides fallback state if Mapbox token is absent so application runs without crashing.
 */
object MapboxConfig {

    val accessToken: String
        get() = try {
            BuildConfig.MAPBOX_ACCESS_TOKEN.ifBlank { "" }
        } catch (_: Throwable) {
            ""
        }

    val isConfigured: Boolean
        get() = accessToken.isNotBlank() &&
                !accessToken.contains("your-mapbox-public-token-here") &&
                (accessToken.startsWith("pk.") || accessToken.startsWith("sk."))

    fun getConfigurationStatus(): String {
        return when {
            isConfigured -> "Mapbox Ready"
            accessToken.isBlank() -> "Mapbox Token Missing (Vector Fallback Active)"
            else -> "Mapbox Placeholder (Vector Fallback Active)"
        }
    }
}
