package com.example.data.remote

import com.example.BuildConfig

/**
 * Clean Supabase configuration manager.
 * Safely reads credentials injected by Secrets Gradle plugin via BuildConfig (.env / .env.example).
 * Never exposes service-role keys.
 * Never crashes if credentials are unset or placeholders.
 */
object SupabaseConfig {

    val url: String
        get() = try {
            BuildConfig.SUPABASE_URL.ifBlank { "" }
        } catch (_: Throwable) {
            ""
        }

    val anonKey: String
        get() = try {
            BuildConfig.SUPABASE_ANON_KEY.ifBlank { "" }
        } catch (_: Throwable) {
            ""
        }

    val isConfigured: Boolean
        get() = url.isNotBlank() &&
                !url.contains("your-project.supabase.co") &&
                anonKey.isNotBlank() &&
                !anonKey.contains("your-supabase-anon-key-here")

    fun getConfigurationStatus(): String {
        return when {
            isConfigured -> "Supabase Connected"
            url.isBlank() || anonKey.isBlank() -> "Supabase Unconfigured (Local-first Offline Mode Active)"
            else -> "Supabase Default Placeholder (Local-first Mode)"
        }
    }
}
