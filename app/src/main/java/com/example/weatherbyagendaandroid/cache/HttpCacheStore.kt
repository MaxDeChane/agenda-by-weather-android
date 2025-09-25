package com.example.weatherbyagendaandroid.cache

import android.content.Context

class HttpCacheStore(private val context: Context) {

    private val prefs = context.getSharedPreferences("forecast_cache", Context.MODE_PRIVATE)

    fun save(url: String, body: String, lastModified: String?) {
        // Save body to a file
        val fileName = urlToFileName(url)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(body.toByteArray())
        }

        // Save last modified header
        prefs.edit().apply {
            lastModified?.let { putString("${url}_modified", it) }
            putString("${url}_file", fileName)
            apply()
        }
    }

    fun getBody(url: String): String {
        val fileName = prefs.getString("${url}_file", null) ?: return ""
        return try {
            context.openFileInput(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    fun getLastModified(url: String): String? {
        return prefs.getString("${url}_modified", null)
    }

    private fun urlToFileName(url: String): String {
        // Simple hash so we don’t have invalid filename chars
        return url.hashCode().toString() + ".json"
    }
}
