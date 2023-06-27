package com.aureusapps.android.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.DigestInputStream
import java.security.MessageDigest

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
 * Retrieves the file name of the content represented by the Uri.
 *
 * @param context The android context.
 *
 * @return The file name of the content, or null if the file name cannot be determined or error occurred.
 */
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
 * Creates a new directory with the given name and returns the Uri of the created directory.
 *
 * @param context The android context.
 * @param dirName The name of the directory to be created.
 *
 * @return The Uri of the created directory, or null if the directory could not be created.
 */
fun Uri.createDirectory(context: Context, dirName: String): Uri? {
    var dirUri: Uri? = null
    try {
        when {
            isFileUri -> {
                val path = path
                if (path != null) {
                    val file = File(path, dirName)
                    if (file.isDirectory || file.mkdirs()) {
                        dirUri = file.toUri()
                    }
                }
            }

            isTreeUri -> {
                val file = DocumentFile.fromTreeUri(context, this)
                if (file != null) {
                    val dir = file.findFile(dirName)
                    if (dir != null && dir.isDirectory) {
                        dirUri = dir.uri

                    } else {
                        val newFile = file.createDirectory(dirName)
                        if (newFile != null) {
                            dirUri = newFile.uri
                        }
                    }
                }
            }
        }

    } catch (_: Exception) {

    }
    return dirUri
}

/**
 * Deletes the file or directory represented by this [Uri] and all its contents recursively.
 *
 * @param context The android context.
 *
 * @return `true` if the deletion is successful, `false` otherwise.
 */
fun Uri.deleteRecursively(context: Context): Boolean {
    var deleted = false
    try {
        when {
            ContentResolver.SCHEME_FILE == scheme -> {
                val path = path
                if (path != null) {
                    val file = File(path)
                    deleted = file.deleteRecursively()
                }
            }

            ContentResolver.SCHEME_CONTENT == scheme -> {
                val file = DocumentFile.fromSingleUri(context, this)
                if (file != null) {
                    deleted = file.delete()
                }
            }

            isTreeUri -> {
                val file = DocumentFile.fromTreeUri(context, this)
                if (file != null) {
                    deleted = file.delete()
                }
            }
        }
    } catch (_: Exception) {

    }
    return deleted
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
 * Copies the content from the source Uri to the destination Uri.
 *
 * @param context The context used to open InputStream and OutputStream from the Uris.
 * @param dstUri The destination Uri to write the content to.
 *
 * @return The total number of bytes written, or -1 if an error occurred.
 */
fun Uri.copyTo(context: Context, dstUri: Uri): Int {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null

    try {
        outputStream = context.contentResolver.openOutputStream(dstUri)
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
                val response = client
                    .newCall(request)
                    .execute()
                val body = response.body
                if (body != null) {
                    if (response.code == 200) {
                        // closing input stream will also close the response
                        inputStream = body.byteStream()
                    } else {
                        // don't call close if body is null
                        response.closeQuietly()
                    }
                }
            }
        }

        var totalBytesWritten = 0
        if (inputStream != null && outputStream != null) {
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesWritten += bytesRead
            }
            outputStream.flush()
            return totalBytesWritten
        }

    } catch (_: Exception) {

    } finally {
        inputStream?.close()
        outputStream?.close()
    }
    return -1
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
                val response = client
                    .newCall(request)
                    .execute()
                val body = response.body
                if (body != null) {
                    if (response.code == 200) {
                        inputStream = body.byteStream()
                    } else {
                        response.closeQuietly()
                    }
                }
            }
        }
        if (inputStream != null) {
            return inputStream.readBytes()
        }

    } catch (_: Exception) {

    } finally {
        inputStream?.close()
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
fun Uri.readToBuffer(context: Context): ByteBuffer? = readBytes(context)
    ?.let { bytes ->
        ByteBuffer
            .allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .put(bytes)
            .position(0) as ByteBuffer
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
                val response = client
                    .newCall(request)
                    .execute()
                val body = response.body
                if (body != null) {
                    if (response.code == 200) {
                        inputStream = body.byteStream()
                    } else {
                        response.closeQuietly()
                    }
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