package com.aureusapps.android.extensions

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL

private const val ERROR_EMPTY_RESPONSE = "Received empty response."
private const val ERROR_NETWORK_REQUEST_FAILED = "Network request failed."

private val httpClient by lazy {
    OkHttpClient()
}

private suspend fun URL.sendNetworkRequest(client: OkHttpClient = httpClient): Response {
    val request = Request.Builder().url(this).build()
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine {
            val call = client.newCall(request)
            call.enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        it.resumeWith(Result.failure(e))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        it.resumeWith(Result.success(response))
                    }
                }
            )
            it.invokeOnCancellation { call.cancel() }
        }
    }
}

suspend fun URL.readFile(context: Context, dstUri: Uri, client: OkHttpClient = httpClient) {
    withContext(Dispatchers.IO) {
        val response = sendNetworkRequest(client)
        if (response.code == 200) {
            val body = response.body ?: throw Exception(ERROR_EMPTY_RESPONSE)
            val inputStream = body.byteStream()
            inputStream.writeTo(dstUri, context)
        } else {
            throw IOException(ERROR_NETWORK_REQUEST_FAILED)
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun URL.readString(client: OkHttpClient = httpClient): String {
    return with(Dispatchers.IO) {
        val response = sendNetworkRequest(client)
        if (response.code == 200) {
            val body = response.body ?: throw Exception(ERROR_EMPTY_RESPONSE)
            body.string()
        } else {
            throw IOException(ERROR_NETWORK_REQUEST_FAILED)
        }
    }
}

suspend fun URL.readJsonArray(): JSONArray {
    val response = readString()
    return JSONArray(response)
}

suspend fun URL.readJsonObject(): JSONObject {
    val response = readString()
    return JSONObject(response)
}

fun URL.toHttps(): URL {
    return if (protocol == "https") {
        this
    } else {
        URL(toString().replace("http", "https"))
    }
}