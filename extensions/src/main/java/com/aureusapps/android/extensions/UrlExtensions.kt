package com.aureusapps.android.extensions

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import kotlin.coroutines.suspendCoroutine

private const val ERROR_EMPTY_RESPONSE = "Received empty response."
private const val ERROR_NETWORK_REQUEST_FAILED = "Network request failed."
private const val ERROR_FONT_SAVE_FAILURE = "Failed to save font file."

private val client by lazy {
    OkHttpClient()
}

private suspend fun URL.sendNetworkRequest(): Response {
    val request = Request.Builder().url(this).build()
    return withContext(Dispatchers.IO) {
        suspendCoroutine {
            client.newCall(request)
                .enqueue(
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            it.resumeWith(Result.failure(e))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            it.resumeWith(Result.success(response))
                        }
                    }
                )
        }
    }
}

suspend fun URL.downloadFile(context: Context, dstUri: Uri) {
    withContext(Dispatchers.IO) {
        val response = sendNetworkRequest()
        if (response.code == 200) {
            val body = response.body ?: throw Exception(ERROR_EMPTY_RESPONSE)
            val inputStream = body.byteStream()
            if (!inputStream.writeTo(dstUri, context)) {
                throw Exception(ERROR_FONT_SAVE_FAILURE)
            }
        } else {
            throw IOException(ERROR_NETWORK_REQUEST_FAILED)
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun URL.downloadString(): String {
    return with(Dispatchers.IO) {
        val response = sendNetworkRequest()
        if (response.code == 200) {
            val body = response.body ?: throw Exception(ERROR_EMPTY_RESPONSE)
            body.string()
        } else {
            throw IOException(ERROR_NETWORK_REQUEST_FAILED)
        }
    }
}

suspend fun URL.downloadJsonArray(): JSONArray {
    val response = downloadString()
    return JSONArray(response)
}

suspend fun URL.downloadJsonObject(): JSONObject {
    val response = downloadString()
    return JSONObject(response)
}

fun URL.toHttps(): URL {
    return if (protocol == "https") {
        this
    } else {
        URL(toString().replace("http", "https"))
    }
}