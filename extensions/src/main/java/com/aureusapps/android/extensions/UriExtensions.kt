package com.aureusapps.android.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.DigestInputStream
import java.security.MessageDigest

suspend fun Uri.saveTo(context: Context, savePath: String) {
    withContext(Dispatchers.IO) {
        try {
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

                "https",
                "http" -> {
                    downloadTo(savePath)
                }

                else -> throw IllegalStateException("Uri scheme does not supported: $scheme")
            }

        } catch (_: Exception) {

        }
    }
}

fun Uri.fileName(context: Context): String? {
    var fileName: String? = null

    try {
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
                val lastSegment = lastPathSegment
                fileName = if (lastSegment != null && TextUtils.isDigitsOnly(lastSegment)) {
                    context.resources.getResourceEntryName(lastSegment.toInt())
                } else {
                    lastSegment
                }
            }

            ContentResolver.SCHEME_FILE -> {
                val path = path
                fileName = path?.substringAfterLast("/")
            }

            "https",
            "http" -> {
                val path = path
                val lastPathSegment = path?.substringAfterLast("/")
                fileName = lastPathSegment?.substringBefore("?")
            }
        }

    } catch (_: Exception) {

    }

    return fileName
}

suspend fun Uri.downloadTo(savePath: String) {
    withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: BufferedInputStream? = null
        var outputStream: BufferedOutputStream? = null

        try {
            val url = this@downloadTo.toString()
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

/**
 * Returns true if the uri is a document tree uri (Uri returned by ACTION_OPEN_DOCUMENT_TREE), otherwise false.
 */
val Uri.isTreeUri: Boolean
    get() {
        val paths = pathSegments
        return paths.size >= 2 && "tree" == paths[0]
    }

/**
 * Returns true if the uri is a file uri, otherwise false.
 */
val Uri.isFileUri: Boolean
    get() = scheme == ContentResolver.SCHEME_FILE

/**
 * Creates a new file inside the directory given by uri.
 * File uris and document tree uris are supported.
 *
 * @param context The Android context.
 * @param displayName The name of the new file.
 * @param mimeType The mime type of the new file.
 *
 * @return Uri of the newly created file or null if the file creation failed.
 */
fun Uri.createFile(
    context: Context,
    displayName: String,
    mimeType: String
): Uri? {
    var fileUri: Uri? = null

    try {
        when {
            isFileUri -> {
                val path = path
                if (path != null) {
                    val file = File(path)
                    if (file.isDirectory) {
                        fileUri = File(file, displayName).toUri()
                    }
                }
            }

            isTreeUri -> {
                fileUri = DocumentFile
                    .fromTreeUri(context, this)
                    ?.createFile(mimeType, displayName)
                    ?.uri
            }
        }
    } catch (_: Exception) {

    }

    return fileUri
}

/**
 * Returns files contained in the directory represented by this Uri.
 *
 * @param context The Android context.
 *
 * @return A list of files contained in the directory represented by this Uri
 * or null if error occurred.
 */
fun Uri.listFiles(context: Context): List<Uri>? {
    var uriList: List<Uri>? = null

    try {
        when {
            isFileUri -> {
                val path = path
                if (path != null) {
                    val file = File(path)
                    if (file.isDirectory) {
                        uriList = file
                            .listFiles()
                            ?.map { it.toUri() }
                    }
                }
            }

            isTreeUri -> {
                val file = DocumentFile.fromTreeUri(context, this)
                if (file != null) {
                    uriList = file
                        .listFiles()
                        .map { it.uri }
                }
            }
        }

    } catch (_: Exception) {

    }

    return uriList
}

/**
 * Returned whether file with the given name exists in the directory represented by the given Uri.
 *
 * @param context The Android context object.
 * @param fileName The name of the file to match.
 *
 * @return 1 if file exists, 0 if file does not exist or -1 if error.
 */
fun Uri.fileExists(context: Context, fileName: String): Int {
    var status = -1

    try {
        when {
            isFileUri -> {
                val path = path
                if (path != null) {
                    val parent = File(path)
                    if (parent.isDirectory) {
                        val file = File(parent, fileName)
                        status = if (file.exists()) 1 else 0
                    }
                }
            }

            isTreeUri -> {
                val file = DocumentFile.fromTreeUri(context, this)
                if (file != null) {
                    val exists = file.listFiles().any { it.name == fileName }
                    status = if (exists) 1 else 0
                }
            }
        }

    } catch (_: Exception) {

    }

    return status
}

/**
 * Reads the contents of the Uri to a byte array.
 *
 * @param context The context used to access the content resolver.
 *
 * @return A byte array containing the contents of the Uri, or null if the operation fails.
 */
fun Uri.readBytes(context: Context): ByteArray? {
    var inputStream: InputStream? = null
    var response: Response? = null

    try {
        when (scheme) {
            ContentResolver.SCHEME_CONTENT,
            ContentResolver.SCHEME_FILE,
            ContentResolver.SCHEME_ANDROID_RESOURCE -> {
                inputStream = context.contentResolver.openInputStream(this)
            }

            "http",
            "https" -> {
                val request = Request.Builder()
                    .url(this.toString())
                    .build()
                val client = OkHttpClient
                    .Builder()
                    .build()
                response = client
                    .newCall(request)
                    .execute()
                val body = response.body
                if (body != null && response.code == 200) {
                    inputStream = body.byteStream()
                }
            }
        }
        if (inputStream != null) {
            return inputStream.readBytes()
        }

    } catch (_: Exception) {

    } finally {
        inputStream?.close()
        response?.close()
    }

    return null
}

/**
 * Reads the contents of the Uri to a ByteBuffer.
 *
 * @param context The context used to access the content resolver.
 *
 * @return A ByteBuffer containing the contents of the Uri, or null if the operation fails.
 */
fun Uri.readBytesToBuffer(context: Context): ByteBuffer? = readBytes(context)
    ?.let { bytes ->
        ByteBuffer
            .allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .put(bytes)
    }

/**
 * Generates a hash from the content of the Uri.
 *
 * @param context The context used to open an InputStream from the Uri.
 * @param algorithm The hash algorithm to use (e.g., "MD5" or "SHA-1").
 *
 * @return The hash string or null if an error occurred.
 */
fun Uri.generateHash(context: Context, algorithm: String): String? {
    var inputStream: InputStream? = null
    var response: Response? = null
    try {
        when (scheme) {
            ContentResolver.SCHEME_CONTENT,
            ContentResolver.SCHEME_FILE,
            ContentResolver.SCHEME_ANDROID_RESOURCE -> {
                inputStream = context.contentResolver.openInputStream(this)
            }

            "http",
            "https" -> {
                val request = Request.Builder()
                    .url(toString())
                    .build()
                val client = OkHttpClient
                    .Builder()
                    .build()
                response = client
                    .newCall(request)
                    .execute()
                val body = response.body
                if (body != null && response.code == 200) {
                    inputStream = body.byteStream()
                }
            }
        }

        if (inputStream != null) {
            val digest = MessageDigest.getInstance(algorithm)
            val digestInputStream = DigestInputStream(inputStream, digest)
            val buffer = ByteArray(8192)
            while (digestInputStream.read(buffer) != -1) {
                // Nothing to do here
            }
            val sha1Bytes = digest.digest()
            val builder = StringBuilder()
            for (byte in sha1Bytes) {
                builder.append(String.format("%02x", byte))
            }
            return builder.toString()
        }

    } catch (_: Exception) {

    } finally {
        inputStream?.close()
        response?.close()
    }
    return null
}

/**
 * Generates an MD5 hash from the content of the Uri.
 *
 * @param context The context used to open an InputStream from the Uri.
 *
 * @return The MD5 hash string or null if an error occurred.
 */
fun Uri.generateMD5(context: Context): String? = generateHash(context, "MD5")

/**
 * Generates a SHA-1 hash from the content of the Uri.
 *
 * @param context The context used to open an InputStream from the Uri.
 * @return The SHA-1 hash string or null if an error occurred.
 */
fun Uri.generateSHA1(context: Context): String? = generateHash(context, "SHA-1")