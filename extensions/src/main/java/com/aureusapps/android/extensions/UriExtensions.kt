package com.aureusapps.android.extensions

import android.annotation.SuppressLint
import android.content.ContentResolver.SCHEME_ANDROID_RESOURCE
import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.aureusapps.android.providerfile.ProviderFile
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class UriExtensionErrors(val message: String) {
    PARENT_FILE_NOT_FOUND("Parent file does not exist."),
    FILE_ALREADY_EXIST("File already exist."),
    FAILED_TO_OVERWRITE("Tried to overwrite the file. But failed to delete it."),
    FILE_CREATION_FAILED("Failed to create file."),
    UNSUPPORTED_URI("Given uri is unsupported."),
    EXCEPTION_OCCURRED("Exception occurred.")
}

data class UnsupportedUriException(
    val uri: Uri,
    override val message: String = "Unsupported uri",
) : Exception(message)

/**
 * Returns true if the uri is a document tree uri (Uri returned by ACTION_OPEN_DOCUMENT_TREE), otherwise false.
 */
val Uri.isTreeUri: Boolean
    get() {
        // content://com.example/tree/12/document/24/
        // content://com.example/tree/12/document/24/children/
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DocumentsContract.isTreeUri(this)
        } else {
            false
        }
    }

/**
 * Test if the given URI represents specific root backed by [DocumentsProvider].
 */
@Suppress("unused")
fun Uri.isRootUri(context: Context): Boolean {
    // content://com.example/root/
    // content://com.example/root/sdcard/
    // content://com.example/root/sdcard/recent/
    // content://com.example/root/sdcard/search/?query=pony
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        DocumentsContract.isRootUri(context, this)
    } else {
        false
    }
}

/**
 * Test if the given URI represents a [DocumentsContract.Document] backed by a DocumentsProvider.
 */
@Suppress("unused")
fun Uri.isDocumentUri(context: Context): Boolean {
    // content://com.example/document/12/
    // content://com.example/tree/12/document/24/
    return DocumentsContract.isDocumentUri(context, this)
}

/**
 * Returns true if the uri is a file uri, otherwise false.
 */
val Uri.isFileUri: Boolean
    get() = scheme == SCHEME_FILE

/**
 * Retrieves a value from the specified column in the content Uri using a cursor.
 *
 * @param context The context of the application or activity.
 * @param column The name of the column from which to retrieve the value.
 * @param defaultValue The default value to return if the specified column is not found or if the cursor is empty.
 * @param block A lambda function that takes a Cursor as its argument and returns the value of the specified column.
 *
 * @return The value retrieved from the specified column in the content URI.
 */
inline fun <T> Uri.queryForValue(
    context: Context,
    column: String,
    defaultValue: T,
    block: (Cursor) -> T,
): T {
    val resolver = context.contentResolver
    var cursor: Cursor? = null
    return try {
        cursor = resolver.query(this, arrayOf(column), null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            block(cursor)
        } else {
            defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    } finally {
        cursor?.closeQuietly()
    }
}

/**
 * Queries the content represented by the Uri for an integer value from the specified column.
 *
 * @param context The context used to access the content resolver.
 * @param column The name of the column from which to retrieve the integer value.
 * @param defaultValue The default value to return if the query does not yield a valid result.
 * @return The integer value from the specified column, or the defaultValue if not found or invalid.
 */
@SuppressLint("Recycle")
@Suppress("unused")
fun Uri.queryForInt(
    context: Context,
    column: String,
    defaultValue: Int,
): Int = queryForValue(context, column, defaultValue) { it.getInt(0) }

/**
 * Queries the content represented by the Uri for a long value from the specified column.
 *
 * @param context The context used to access the content resolver.
 * @param column The name of the column from which to retrieve the long value.
 * @param defaultValue The default value to return if the query does not yield a valid result.
 * @return The long value from the specified column, or the defaultValue if not found or invalid.
 */
fun Uri.queryForLong(
    context: Context,
    column: String,
    defaultValue: Long,
): Long = queryForValue(context, column, defaultValue) { it.getLong(0) }

/**
 * Queries the content represented by the Uri for a string value from the specified column.
 *
 * @param context The context used to access the content resolver.
 * @param column The name of the column from which to retrieve the string value.
 * @param defaultValue The default value to return if the query does not yield a valid result.
 * @return The string value from the specified column, or the defaultValue if not found or invalid.
 */
@Suppress("unused")
fun Uri.queryForString(
    context: Context,
    column: String,
    defaultValue: String,
): String = queryForValue(context, column, defaultValue) { it.getString(0) }

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
            SCHEME_CONTENT,
            SCHEME_FILE,
            -> {
                fileName = ProviderFile.fromUri(context, this)?.name
            }

            SCHEME_ANDROID_RESOURCE -> {
                val segments = pathSegments
                if (segments.size >= 1) {
                    val lastSegment = segments.last()
                    if (TextUtils.isDigitsOnly(lastSegment)) {
                        fileName = context
                            .resources
                            .getResourceEntryName(lastSegment.toInt())

                    } else if (segments.size >= 2) {
                        fileName = lastSegment
                    }
                }
            }

            "https",
            "http",
            -> {
                fileName = path
                    ?.substringAfterLast("/")
                    ?.substringBefore("?")
            }
        }
    } catch (_: Exception) {
    }
    return fileName
}

/**
 * Checks if the Uri represents a directory.
 *
 * @param context The context used for accessing content resolver.
 * @return `true` if the Uri represents a directory, `false` otherwise.
 */
fun Uri.isDirectory(context: Context): Boolean {
    var result = false
    try {
        when (scheme) {
            SCHEME_FILE -> {
                result = path
                    ?.let { File(it) }
                    ?.isDirectory ?: false
            }

            SCHEME_CONTENT -> {
                result = ProviderFile
                    .fromUri(context, this)
                    ?.isDirectory ?: false
            }
        }
    } catch (_: Exception) {
    }
    return result
}

/**
 * Returns files contained in the directory represented by this Uri.
 *
 * @param context The Android context.
 *
 * @return A list of files contained in the directory represented by this Uri
 * or null if the given [Uri] does not represent a directory.
 */
fun Uri.listFiles(context: Context): List<Uri>? {
    var uriList: List<Uri>? = null
    try {
        when (scheme) {
            SCHEME_FILE -> {
                uriList = path
                    ?.let { File(it) }
                    ?.takeIf { it.isDirectory }
                    ?.listFiles()
                    ?.map { it.toUri() }
            }

            SCHEME_CONTENT -> {
                uriList = ProviderFile.fromUri(context, this)
                    ?.listFiles()
                    ?.map { it.uri }
            }
        }
    } catch (_: Exception) {
    }
    return uriList
}

/**
 * Checks if the directory represented by the Uri is empty.
 *
 * @param context The context used for accessing content resolver.
 * @return `true` if the directory is empty, `false` otherwise.
 */
fun Uri.isEmpty(context: Context): Boolean {
    var empty = false
    try {
        empty = listFiles(context)?.isEmpty() ?: false
    } catch (_: Exception) {
    }
    return empty
}

/**
 * Creates a new file inside the directory given by uri.
 *
 * @param context The Android context.
 * @param fileName The name of the new file.
 *
 * @return Uri of the newly created file or null if the file creation failed.
 */
fun Uri.createFile(
    context: Context,
    fileName: String,
    overwrite: Boolean = false,
    onError: (UriExtensionErrors) -> Unit = {},
): Uri? {
    var fileUri: Uri? = null
    try {
        when (scheme) {
            SCHEME_FILE -> {
                val parentFile = toFile()
                if (parentFile.exists()) {
                    val childFile = File(parentFile, fileName)
                    val createFile = if (childFile.exists()) {
                        if (overwrite) {
                            val stillExists = if (childFile.isDirectory) {
                                !childFile.deleteRecursively()
                            } else {
                                !childFile.delete()
                            }
                            if (stillExists) {
                                onError(UriExtensionErrors.FAILED_TO_OVERWRITE)
                            }
                            !stillExists
                        } else {
                            onError(UriExtensionErrors.FILE_ALREADY_EXIST)
                            false
                        }
                    } else {
                        true
                    }
                    if (createFile) {
                        if (childFile.createNewFile()) {
                            fileUri = childFile.toUri()
                        } else {
                            onError(UriExtensionErrors.FILE_CREATION_FAILED)
                        }
                    }
                } else {
                    onError(UriExtensionErrors.PARENT_FILE_NOT_FOUND)
                }
            }

            SCHEME_CONTENT -> {
                val parentDoc = ProviderFile.fromUri(context, this)
                if (parentDoc != null && parentDoc.exists()) {
                    val existing = parentDoc.findFile(fileName)
                    val createFile = if (existing != null) {
                        if (overwrite) {
                            if (existing.delete()) {
                                true
                            } else {
                                onError(UriExtensionErrors.FAILED_TO_OVERWRITE)
                                false
                            }
                        } else {
                            onError(UriExtensionErrors.FILE_ALREADY_EXIST)
                            false
                        }
                    } else {
                        true
                    }
                    if (createFile) {
                        val extension = fileName.substringAfterLast(".")
                        val mimeType = MimeTypeMap
                            .getSingleton()
                            .getMimeTypeFromExtension(extension)
                            ?: "application/octet-stream"
                        val childDoc = parentDoc.createFile(mimeType, fileName)
                        if (childDoc != null) {
                            fileUri = childDoc.uri
                        } else {
                            onError(UriExtensionErrors.FILE_CREATION_FAILED)
                        }
                    }
                } else {
                    onError(UriExtensionErrors.PARENT_FILE_NOT_FOUND)
                }
            }

            else -> {
                onError(UriExtensionErrors.UNSUPPORTED_URI)
            }
        }
    } catch (e: Exception) {
        onError(UriExtensionErrors.EXCEPTION_OCCURRED)
    }
    return fileUri
}

/**
 * Creates a new directory with the given name and returns the Uri of the created directory.
 *
 * @param context The android context.
 * @param dirName The name of the directory to be created.
 * @param overwrite If set to true and the directory already exists, it will be overwritten.
 * @param onError A callback function that is invoked when an error occurs during the process. It receives an instance of [UriExtensionErrors].
 *
 * @return The Uri of the created directory or existing directory, or null if the directory could not be created.
 */
fun Uri.createDirectory(
    context: Context,
    dirName: String,
    overwrite: Boolean = false,
    onError: (UriExtensionErrors) -> Unit = {},
): Uri? {
    var dirUri: Uri? = null
    try {
        when (scheme) {
            SCHEME_FILE -> {
                val parentFile = toFile()
                if (parentFile.exists()) {
                    val childFile = File(parentFile, dirName)
                    val createDir = if (childFile.exists()) {
                        if (childFile.isDirectory) {
                            dirUri = childFile.toUri()
                            false
                        } else {
                            if (overwrite) {
                                val deleted = childFile.delete()
                                if (!deleted) {
                                    onError(UriExtensionErrors.FAILED_TO_OVERWRITE)
                                }
                                deleted
                            } else {
                                onError(UriExtensionErrors.FILE_ALREADY_EXIST)
                                false
                            }
                        }
                    } else {
                        true
                    }
                    if (createDir) {
                        if (childFile.mkdirs()) {
                            dirUri = childFile.toUri()
                        } else {
                            onError(UriExtensionErrors.FILE_CREATION_FAILED)
                        }
                    }
                } else {
                    onError(UriExtensionErrors.PARENT_FILE_NOT_FOUND)
                }
            }

            SCHEME_CONTENT -> {
                val parentDoc = ProviderFile.fromUri(context, this)
                if (parentDoc != null && parentDoc.exists()) {
                    val existing = parentDoc.findFile(dirName)
                    val createDir = if (existing != null) {
                        if (existing.isDirectory) {
                            dirUri = existing.uri
                            false
                        } else if (overwrite) {
                            if (existing.delete()) {
                                true
                            } else {
                                onError(UriExtensionErrors.FAILED_TO_OVERWRITE)
                                false
                            }
                        } else {
                            onError(UriExtensionErrors.FILE_ALREADY_EXIST)
                            false
                        }
                    } else {
                        true
                    }
                    if (createDir) {
                        val childDoc = parentDoc.createDirectory(dirName)
                        if (childDoc != null) {
                            dirUri = childDoc.uri
                        } else {
                            onError(UriExtensionErrors.FILE_CREATION_FAILED)
                        }
                    }
                } else {
                    onError(UriExtensionErrors.PARENT_FILE_NOT_FOUND)
                }
            }

            else -> {
                onError(UriExtensionErrors.UNSUPPORTED_URI)
            }
        }
    } catch (_: Exception) {
        onError(UriExtensionErrors.EXCEPTION_OCCURRED)
    }
    return dirUri
}

/**
 * Gets or creates a directory with the specified name within the context of the given Uri.
 *
 * This function is used to interact with URIs, such as accessing or creating directories within a specific context.
 *
 * @param context The context in which the Uri is being accessed or created.
 * @param dirName The name of the directory to be accessed or created.
 * @param overwrite If set to true and the directory already exists, it will be overwritten.
 * @param onError A callback function that is invoked when an error occurs during the process. It receives an instance of [UriExtensionErrors].
 * @return The Uri representing the accessed or created directory, or null if an error occurred.
 */
@Suppress("unused")
fun Uri.getOrCreateDirectory(
    context: Context,
    dirName: String,
    overwrite: Boolean = false,
    onError: (UriExtensionErrors) -> Unit = {},
): Uri? {
    val existing = findFile(context, dirName)
    if (existing != null && existing.isDirectory(context)) {
        return existing
    }
    return createDirectory(context, dirName, overwrite, onError)
}

/**
 * Gets or creates a file with the specified name within the context of the given Uri.
 *
 * @param context The android context.
 * @param fileName The name of the file to be accessed or created.
 * @param overwrite If set to true and the file already exists, it will be overwritten.
 *
 * @return The Uri representing the accessed or created file, or null if an error occurred.
 */
fun Uri.getOrCreateFile(
    context: Context,
    fileName: String,
    overwrite: Boolean = false,
): Uri? {
    val existing = findFile(context, fileName)
    if (existing != null && !existing.isDirectory(context)) {
        return existing
    }
    return createFile(context, fileName, overwrite)
}

/**
 * Checks if the resource referenced by the Uri exists.
 *
 * @param context The context used for accessing resources and content resolver.
 *
 * @return `true` if the resource exists, `false` otherwise.
 */
@SuppressLint("DiscouragedApi")
fun Uri.exists(context: Context): Boolean {
    var exists = false
    try {
        when (scheme) {
            SCHEME_FILE -> {
                exists = path
                    ?.let { File(it) }
                    ?.exists() ?: false
            }

            SCHEME_CONTENT -> {
                exists = ProviderFile
                    .fromUri(context, this)
                    ?.exists() ?: false
            }

            SCHEME_ANDROID_RESOURCE -> {
                val segments = pathSegments
                if (segments.size >= 1) {
                    val lastSegment = segments.last()
                    if (TextUtils.isDigitsOnly(lastSegment)) {
                        context
                            .resources
                            .getResourceEntryName(lastSegment.toInt())
                        exists = true

                    } else if (segments.size >= 2) {
                        val resourceType = segments[0]
                        val packageName = authority
                        val resourceId = context
                            .resources
                            .getIdentifier(lastSegment, resourceType, packageName)
                        if (resourceId != 0) {
                            exists = true
                        }
                    }
                }
            }

            "http",
            "https",
            -> {
                val url = URL(toString())
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                exists = connection.responseCode == 200
                connection.disconnect()
            }
        }
    } catch (_: Exception) {
    }
    return exists
}

/**
 * Deletes the file or directory represented by this [Uri] and all its contents recursively.
 *
 * @param context The android context.
 *
 * @return `true` if the deletion is successful, `false` otherwise.
 */
fun Uri.delete(context: Context): Boolean {
    var deleted = false
    try {
        when (scheme) {
            SCHEME_FILE -> {
                deleted = path
                    ?.let { File(it) }
                    ?.deleteRecursively() ?: false
            }

            SCHEME_CONTENT -> {
                deleted = ProviderFile
                    .fromUri(context, this)
                    ?.delete() ?: false
            }
        }

    } catch (_: Exception) {

    }
    return deleted
}

/**
 * Finds a file with the given name within the directory represented by the Uri.
 *
 * @param context The context used for accessing content resolver.
 * @param fileName The name of the file to search for.
 * @return The Uri of the found file, or null if the file is not found or an error occurs.
 */
fun Uri.findFile(context: Context, fileName: String): Uri? {
    var uri: Uri? = null
    try {
        when (scheme) {
            SCHEME_FILE -> {
                uri = path
                    ?.let { File(it) }
                    ?.listFiles()
                    ?.firstOrNull { it.name == fileName }
                    ?.toUri()
            }

            SCHEME_CONTENT -> {
                uri = ProviderFile.fromUri(context, this)
                    ?.findFile(fileName)
                    ?.uri
            }
        }
    } catch (_: Exception) {
    }
    return uri
}

/**
 * Finds a directory with the given name within the directory represented by the Uri.
 *
 * @param context The context in which the directory search will be performed.
 * @param dirName The name of the directory to be found.
 *
 * @return The Uri of the directory if found within the context, or null if not found.
 */
fun Uri.findDirectory(context: Context, dirName: String): Uri? {
    var uri: Uri? = null
    try {
        when (scheme) {
            SCHEME_FILE -> {
                uri = path
                    ?.let { File(it) }
                    ?.listFiles()
                    ?.firstOrNull { it.name == dirName }
                    ?.takeIf { it.isDirectory }
                    ?.toUri()
            }

            SCHEME_CONTENT -> {
                uri = ProviderFile.fromUri(context, this)
                    ?.findFile(dirName)
                    ?.takeIf { it.isDirectory }
                    ?.uri
            }
        }
    } catch (_: Exception) {
    }
    return uri
}

/**
 * Returns the size of the content represented by the Uri in bytes.
 *
 * @return The size of the content in bytes, or 0 if the size cannot be determined.
 */
fun Uri.fileSize(context: Context): Long {
    return when (scheme) {
        SCHEME_FILE,
        SCHEME_CONTENT,
        -> ProviderFile.fromUri(context, this)?.length() ?: 0

        SCHEME_ANDROID_RESOURCE -> context
            .contentResolver
            .openAssetFileDescriptor(this, "r")
            ?.use { it.length }
            ?: 0

        "http",
        "https",
        -> {
            val url = toString()
            val con = URL(url).openConnection() as HttpURLConnection
            con.connectTimeout = 20000
            con.readTimeout = 20000
            con.connect()
            val len = con.contentLength.toLong()
            con.disconnect()
            len
        }

        else -> 0
    }
}

/**
 * Opens an input stream for reading the content represented by the Uri.
 *
 * @param context The context used for accessing content resolver.
 *
 * @return An InputStream for reading the content, or null if the content cannot be opened.
 */
@SuppressLint("Recycle")
fun Uri.openInputStream(context: Context): InputStream? {
    var inputStream: InputStream? = null
    try {
        when (scheme) {
            SCHEME_CONTENT,
            SCHEME_FILE,
            SCHEME_ANDROID_RESOURCE,
            -> {
                inputStream = context.contentResolver.openInputStream(this)
            }

            "http",
            "https",
            -> {
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
                        inputStream = body.byteStream().onClose { response.close() }
                    } else {
                        response.close()
                    }
                }
            }
        }
    } catch (_: Exception) {
    }
    return inputStream
}

private fun InputStream.onClose(block: () -> Unit): InputStream {
    return object : InputStream() {
        override fun read(): Int = this@onClose.read()
        override fun read(b: ByteArray?): Int = this@onClose.read(b)
        override fun read(b: ByteArray?, off: Int, len: Int): Int = this@onClose.read(b, off, len)

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun readAllBytes(): ByteArray = this@onClose.readAllBytes()

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun readNBytes(len: Int): ByteArray = this@onClose.readNBytes(len)

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun readNBytes(b: ByteArray?, off: Int, len: Int): Int =
            this@onClose.readNBytes(b, off, len)

        override fun skip(n: Long): Long = this@onClose.skip(n)
        override fun available(): Int = this@onClose.available()
        override fun mark(readlimit: Int) = this@onClose.mark(readlimit)
        override fun reset() = this@onClose.reset()
        override fun markSupported(): Boolean = this@onClose.markSupported()

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun transferTo(out: OutputStream?): Long = this@onClose.transferTo(out)

        override fun close() {
            this@onClose.close()
            block()
        }

        override fun equals(other: Any?): Boolean = this@onClose == other
        override fun hashCode(): Int = this@onClose.hashCode()
        override fun toString(): String = this@onClose.toString()
    }
}

/**
 * Copies the content represented by the source Uri to a target parent Uri using the provided context.
 *
 * @param context The context used to access the content resolver.
 * @param targetParent The parent Uri to which the content should be copied.
 * @param overwrite Determines whether to overwrite the target content if it already exists. Default is false.
 * @param onError A lambda function that handles errors during the copy operation.
 *                It takes an error message as input and returns a boolean indicating whether
 *                the operation should be terminated (true) or continued (false). Default is to terminate.
 * @return true if the copy operation is successful, false otherwise.
 */
fun Uri.copyTo(
    context: Context,
    targetParent: Uri,
    overwrite: Boolean = false,
    onError: (String) -> Boolean = { true },
): Boolean {
    val srcScheme = this.scheme
    val dstScheme = targetParent.scheme
    val srcDocument = when (srcScheme) {
        SCHEME_FILE,
        SCHEME_CONTENT,
        -> ProviderFile.fromUri(context, this)

        SCHEME_ANDROID_RESOURCE,
        "http",
        "https",
        -> null

        else -> {
            onError("Unsupported source uri.")
            return false
        }
    }
    val dstDocument = when (dstScheme) {
        SCHEME_FILE,
        SCHEME_CONTENT,
        -> ProviderFile.fromUri(context, targetParent)

        else -> {
            onError("Unsupported destination uri.")
            return false
        }
    }
    if (srcDocument != null && dstDocument != null) {
        return srcDocument.copyTo(context, dstDocument, overwrite, onError)
    } else {
        // open source input stream
        val inputStream = this.openInputStream(context) ?: run {
            onError("Given source uri is not supported.")
            return false
        }
        val srcName = this.fileName(context) ?: run {
            onError("Failed to retrieve source file name.")
            return false
        }
        val extension = srcName.substringAfterLast(".")
        val mimeType = if (targetParent.scheme != SCHEME_FILE) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""
        } else {
            ""
        }
        // create destination file
        val dstFile = dstDocument?.createFile(mimeType, srcName) ?: run {
            onError("Failed to create destination file.")
            return false
        }
        val outputStream = dstFile.openOutputStream(context) ?: run {
            onError("Failed to open destination output stream.")
            return false
        }
        try {
            val fileSize = this.fileSize(context)
            return inputStream.copyTo(outputStream) == fileSize
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }
}

/**
 * Move files recursively from this uri to the [dstUri].
 */
fun Uri.moveTo(
    context: Context,
    dstUri: Uri,
    overwrite: Boolean = false,
    onError: (Exception) -> OnErrorAction = { OnErrorAction.TERMINATE },
): Boolean {
    val srcFile = ProviderFile.fromUri(context, this)
        ?: return onError(UnsupportedUriException(this)) != OnErrorAction.TERMINATE
    val dstFile = ProviderFile.fromUri(context, dstUri)
        ?: return onError(UnsupportedUriException(dstUri)) != OnErrorAction.TERMINATE
    return srcFile.moveTo(context, dstFile, overwrite, onError)
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
    var bytes: ByteArray? = null
    try {
        inputStream = openInputStream(context)
        bytes = inputStream?.readBytes()
    } catch (_: Exception) {
    } finally {
        inputStream?.closeQuietly()
    }
    return bytes
}

/**
 * Writes the given byte array to the content represented by the Uri.
 *
 * @param context The context used for accessing content resolver.
 * @param bytes The byte array to write.
 * @return `true` if the write operation is successful, `false` otherwise.
 */
@SuppressLint("Recycle")
fun Uri.writeBytes(context: Context, bytes: ByteArray): Boolean {
    var result = false
    var outputStream: OutputStream? = null
    try {
        outputStream = context.contentResolver.openOutputStream(this)
            ?: throw NullPointerException("Could not open the output stream.")
        outputStream.write(bytes)
        outputStream.flush()
        result = true
    } catch (_: Exception) {
    } finally {
        outputStream?.closeQuietly()
    }
    return result
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