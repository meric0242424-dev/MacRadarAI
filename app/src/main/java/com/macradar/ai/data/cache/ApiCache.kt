package com.macradar.ai.data.cache

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Simple disk-backed cache (SharedPreferences + Gson) for API responses,
 * to minimize requests against the limited free api-football.com plan.
 *
 * Each entry is stored with its own expiry timestamp, so different data
 * types (fixtures, team stats, predictions) can have different TTLs.
 */
class ApiCache(context: Context) {

    private val prefs = context.getSharedPreferences("api_cache_v1", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val TTL_FIXTURES_MS = 15 * 60 * 1000L          // 15 minutes (scores change)
        const val TTL_TEAM_STATS_MS = 24 * 60 * 60 * 1000L   // 24 hours (rarely changes)
        const val TTL_PREDICTION_MS = 12 * 60 * 60 * 1000L   // 12 hours
        const val TTL_H2H_MS = 24 * 60 * 60 * 1000L          // 24 hours
    }

    private data class CacheEntry(val json: String, val expiresAt: Long)

    fun <T> get(key: String, type: java.lang.reflect.Type): T? {
        val raw = prefs.getString(key, null) ?: return null
        return try {
            val entry = gson.fromJson(raw, CacheEntry::class.java)
            if (System.currentTimeMillis() > entry.expiresAt) {
                prefs.edit().remove(key).apply()
                null
            } else {
                gson.fromJson(entry.json, type)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun <T> put(key: String, value: T, ttlMs: Long) {
        try {
            val json = gson.toJson(value)
            val entry = CacheEntry(json, System.currentTimeMillis() + ttlMs)
            prefs.edit().putString(key, gson.toJson(entry)).apply()
        } catch (e: Exception) {
            // Ignore cache write failures, not critical
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

/** Helper inline functions for typed get/put without manual TypeToken boilerplate at call sites. */
inline fun <reified T> ApiCache.getTyped(key: String): T? {
    val type = object : TypeToken<T>() {}.type
    return get(key, type)
}
