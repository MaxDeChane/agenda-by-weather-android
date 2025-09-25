package com.example.weatherbyagendaandroid.interceptor

import com.example.weatherbyagendaandroid.cache.HttpCacheStore
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody

class HttpCacheInterceptor (private val cacheStore: HttpCacheStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Add If-Modified-Since header if we have it
        val lastModified = cacheStore.getLastModified(url)
        val newRequest = if (lastModified != null) {
            request.newBuilder()
                .header("If-Modified-Since", lastModified)
                .build()
        } else {
            request
        }

        val response = chain.proceed(newRequest)

        return when (response.code) {
            200 -> {
                // Store new content + Last-Modified header
                val bodyString = response.body?.string() ?: ""
                cacheStore.save(url, bodyString, response.header("Last-Modified"))
                // Create new response body for Retrofit to consume
                response.newBuilder()
                    .body(ResponseBody.create(response.body?.contentType(), bodyString))
                    .build()
            }
            304 -> {
                // Return cached version as a fake 200
                val cachedBody = cacheStore.getBody(url)
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK (from cache)")
                    .body(ResponseBody.create("application/json".toMediaType(), cachedBody))
                    .build()
            }
            else -> response
        }
    }
}