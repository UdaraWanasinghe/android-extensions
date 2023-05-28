package com.aureusapps.android.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

suspend fun Uri.saveTo(context: Context, path: String) {
    withContext(Dispatchers.IO) {
        when (scheme) {
            ContentResolver.SCHEME_CONTENT,
            ContentResolver.SCHEME_FILE,
            ContentResolver.SCHEME_ANDROID_RESOURCE -> {
                val inputStream = context.contentResolver.openInputStream(this@saveTo)
                    ?: throw IllegalStateException("Content provider crashed")
                val outputStream = FileOutputStream(path)
                inputStream.writeTo(outputStream)
                inputStream.close()
                outputStream.close()
            }

            "https" -> {
                downloadFile(this@saveTo.toString(), path)
            }

            else -> throw IllegalStateException("Uri scheme does not supported: $scheme")
        }
    }
}

private fun downloadFile(url: String, filePath: String) {
    var connection: HttpURLConnection? = null
    var inputStream: BufferedInputStream? = null
    var outputStream: BufferedOutputStream? = null

    try {
        val fileUrl = URL(url)
        connection = fileUrl.openConnection() as HttpURLConnection
        connection.connect()

        // Check if the request was successful
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            inputStream = BufferedInputStream(connection.inputStream)
            outputStream = BufferedOutputStream(FileOutputStream(filePath))

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        } else {
            throw IOException("Failed to download file: $responseCode")
        }
    } finally {
        connection?.disconnect()
        inputStream?.close()
        outputStream?.close()
    }
}
