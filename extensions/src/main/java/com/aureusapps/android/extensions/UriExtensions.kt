package com.aureusapps.android.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

suspend fun Uri.saveTo(context: Context, savePath: String) {
    withContext(Dispatchers.IO) {
        when (scheme) {
            ContentResolver.SCHEME_CONTENT,
            ContentResolver.SCHEME_FILE,
            ContentResolver.SCHEME_ANDROID_RESOURCE -> {
                val inputStream = context.contentResolver.openInputStream(this@saveTo)
                    ?: throw IllegalStateException("Content provider crashed")
                val outputStream = FileOutputStream(savePath)
                inputStream.writeTo(outputStream)
                inputStream.close()
                outputStream.close()
            }

            "https" -> {
                downloadTo(savePath)
            }

            else -> throw IllegalStateException("Uri scheme does not supported: $scheme")
        }
    }
}

fun Uri.fileName(context: Context): String? {
    var fileName: String? = null
    when (scheme) {
        ContentResolver.SCHEME_CONTENT -> {
            val cursor = context.contentResolver.query(
                this,
                arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    fileName = it.getString(columnIndex)
                }
            }
        }

        ContentResolver.SCHEME_ANDROID_RESOURCE -> {
            val resourceId = lastPathSegment
            fileName = context.resources.getResourceEntryName(resourceId!!.toInt())
        }

        ContentResolver.SCHEME_FILE -> {
            val path = path
            fileName = path?.substringAfterLast("/")
        }

        "https" -> {
            val path = path
            val lastPathSegment = path?.substringAfterLast("/")
            fileName = lastPathSegment?.substringBefore("?")
        }
    }
    return fileName
}

suspend fun Uri.downloadTo(savePath: String) {
    withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: BufferedInputStream? = null
        var outputStream: BufferedOutputStream? = null

        try {
            val url = toString()
            val fileUrl = URL(url)
            connection = fileUrl.openConnection() as HttpURLConnection
            connection.connect()

            // Check if the request was successful
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = BufferedInputStream(connection.inputStream)
                outputStream = BufferedOutputStream(FileOutputStream(savePath))

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
}