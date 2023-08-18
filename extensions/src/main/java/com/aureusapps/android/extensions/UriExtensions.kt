package com.aureusapps.android.extensions

import android.annotation.SuppressLint
import android.content.ContentResolver.SCHEME_ANDROID_RESOURCE
import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
 * Retrieves the file name of the content represented by the Uri.
 *
 * @param context The android context.
 *
 * @return The file name of the content, or null if the file name cannot be determined or error occurred.
 */
@SuppressLint("DiscouragedApi")
fun Uri.fileName(context: Context): String? {
    var fileName: String? = null
    try {
        when (scheme) {
            SCHEME_CONTENT -> {
                fileName = DocumentFile.fromSingleUri(context, this)?.name
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

            SCHEME_FILE -> {
                fileName = lastPathSegment
            }

            "https",
            "http" -> {
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
        when {
            isFileUri -> {
                result = path
                    ?.let { File(it) }
                    ?.isDirectory ?: false
            }

            isTreeUri -> {
                result = DocumentFile
                    .fromTreeUri(context, this)
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
        when {
            isFileUri -> {
                uriList = path
                    ?.let { File(it) }
                    ?.takeIf { it.isDirectory }
                    ?.listFiles()
                    ?.map { it.toUri() }
            }

            isTreeUri -> {
                uriList = DocumentFile.fromTreeUri(context, this)
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
                fileUri = path
                    ?.let { File(it) }
                    ?.takeIf { it.isDirectory }
                    ?.let { File(it, displayName) }
                    ?.toUri()
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
 * @return The Uri of the created directory or existing directory, or null if the directory could not be created.
 */
fun Uri.createDirectory(context: Context, dirName: String): Uri? {
    var dirUri: Uri? = null
    try {
        when {
            isFileUri -> {
                dirUri = path
                    ?.let { File(it, dirName) }
                    ?.takeIf { it.isDirectory || it.mkdir() }
                    ?.toUri()
            }

            isTreeUri -> {
                val root = DocumentFile
                    .fromTreeUri(context, this)
                    ?.takeIf { it.isDirectory }
                dirUri = root
                    ?.findFile(dirName)
                    ?.takeIf { it.isDirectory }
                    ?.uri ?: root
                    ?.createDirectory(dirName)
                    ?.uri
            }
        }

    } catch (_: Exception) {

    }
    return dirUri
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
                exists = DocumentFile
                    .fromSingleUri(context, this)
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

            "tree" -> {
                exists = DocumentFile
                    .fromTreeUri(context, this)
                    ?.exists() ?: false
            }

            "http",
            "https" -> {
                val url = URL(toString())
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                exists = connection.responseCode == 200
                connection.disconnect()
            }

            else -> {
                throw Exception("Unknown Uri scheme")
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
        when {
            SCHEME_FILE == scheme -> {
                deleted = path
                    ?.let { File(it) }
                    ?.deleteRecursively() ?: false
            }

            SCHEME_CONTENT == scheme -> {
                deleted = DocumentFile
                    .fromSingleUri(context, this)
                    ?.delete() ?: false
            }

            isTreeUri -> {
                deleted = DocumentFile
                    .fromTreeUri(context, this)
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
        when {
            isFileUri -> {
                uri = path
                    ?.let { File(it) }
                    ?.listFiles()
                    ?.firstOrNull { it.name == fileName }
                    ?.toUri()
            }

            isTreeUri -> {
                uri = DocumentFile.fromTreeUri(context, this)
                    ?.findFile(fileName)
                    ?.uri
            }
        }

    } catch (_: Exception) {

    } finally {

    }
    return uri
}

/**
 * Checks if a file with the given name exists within the directory specified by the Uri.
 *
 * @param context The android context.
 * @param fileName The name of the file to check for existence.
 *
 * @return `true` if the file exists, `false` otherwise.
 */
fun Uri.fileExists(context: Context, fileName: String): Boolean {
    var exists = false
    try {
        when {
            isFileUri -> {
                exists = path
                    ?.let { File(it) }
                    ?.takeIf { it.isDirectory }
                    ?.let { File(it, fileName) }
                    ?.exists() ?: false
            }

            isTreeUri -> {
                exists = DocumentFile
                    .fromTreeUri(context, this)
                    ?.findFile(fileName) != null
            }
        }

    } catch (_: Exception) {

    }
    return exists
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
            SCHEME_ANDROID_RESOURCE -> {
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

    } catch (_: Exception) {

    }
    return inputStream
}

/**
 * Copies the content from the source Uri to the destination Uri.
 *
 * @param context The context used to open InputStream and OutputStream from the Uris.
 * @param dstUri The destination Uri to write the content to.
 * @return The total number of bytes written, or -1 if an error occurred.
 */
fun Uri.copyTo(context: Context, dstUri: Uri): Long {
    var input: InputStream? = null
    var output: OutputStream? = null
    var written = -1L
    try {
        output = context.contentResolver.openOutputStream(dstUri)
        input = openInputStream(context)
        if (input != null && output != null) {
            written = input.copyTo(output)
        }
    } catch (_: Exception) {
    } finally {
        input?.closeQuietly()
        output?.closeQuietly()
    }
    return written
}

/**
 * Recursively copies the contents of the source directory or file specified by this [Uri] to the target
 * directory or file specified by the [targetUri]. If the source [Uri] represents a directory, all its
 * contents, including subdirectories and files, will be copied to the target directory. If the source [Uri]
 * represents a file, only that file will be copied to the target [Uri].
 *
 * @param context The [Context] used to access content resolver.
 * @param targetUri The target [Uri] representing the destination directory or file where the contents
 *                  will be copied to.
 * @param overwrite If set to true and a file with the same name already exists at the target location,
 *                  it will be overwritten. If set to false, the function will not overwrite existing files
 *                  and will throw exception.
 */
fun Uri.copyRecursively(context: Context, targetUri: Uri, overwrite: Boolean = false): Boolean {
    try {
        val srcScheme = scheme
        val targetScheme = targetUri.scheme
        when (srcScheme) {
            SCHEME_FILE -> {
                val srcFile = toFile()
                when {
                    SCHEME_FILE == targetScheme -> {
                        val dstFile = targetUri.toFile()
                        srcFile.copyRecursively(dstFile, overwrite)
                    }

                    targetUri.isTreeUri -> {
                        val targetFile = DocumentFile.fromTreeUri(context, targetUri)
                        if (targetFile != null && targetFile.exists()) {
                            if (srcFile.isDirectory && targetFile.isDirectory || srcFile.isFile && targetFile.isFile) {
                                copyRecursively(context, srcFile, targetFile, overwrite)
                            } else {
                                throw RuntimeException("Incompatible source and target files.")
                            }
                        } else {
                            throw FileNotFoundException("Target file does not exists.")
                        }
                    }

                    else -> {
                        throw UnsupportedOperationException("Destination directory uri scheme ($targetScheme) is not supported")
                    }
                }
            }

            SCHEME_ANDROID_RESOURCE,
            SCHEME_CONTENT,
            "http",
            "https" -> {
                val srcStream = openInputStream(context)
                    ?: throw IOException("Could not open android resource input stream.")
                val dstStream = when {
                    SCHEME_FILE == targetScheme -> {
                        val targetFile = targetUri.toFile()
                        if (targetFile.exists() && targetFile.isFile) {
                            if (!overwrite || !targetFile.delete()) {
                                throw RuntimeException("File already exists.")
                            }
                        }
                        FileOutputStream(targetFile)
                    }

                    SCHEME_CONTENT == targetScheme -> {
                        context.contentResolver.openOutputStream(targetUri)
                            ?: throw IOException("Could not open target output stream")
                    }

                    else -> {
                        throw UnsupportedOperationException("Destination directory uri scheme ($targetScheme) is not supported")
                    }
                }
                dstStream.use { output ->
                    srcStream.use { input -> input.copyTo(output) }
                }
            }

            "tree" -> {
                val srcFile = DocumentFile.fromTreeUri(context, this)
                when (targetScheme) {
                    SCHEME_FILE -> {

                    }

                    "tree" -> {

                    }

                    else -> {
                        throw UnsupportedOperationException("Destination directory uri scheme ($targetScheme) is not supported")
                    }
                }
            }
        }
    } catch (_: Exception) {
        return false
    }
    return true
}

private fun copyRecursively(
    context: Context,
    srcFile: File,
    targetFile: DocumentFile,
    overwrite: Boolean
) {
    if (srcFile.isDirectory) {
        val files = srcFile.listFiles() ?: emptyArray()
        for (file in files) {
            if (file.isDirectory) {
                val foundFile = targetFile.findFile(file.name)
                val dstDir = if (foundFile != null && foundFile.isDirectory) {
                    foundFile
                } else {
                    targetFile.createDirectory(file.name)
                        ?: throw IOException("Could not create directory.")
                }
                copyRecursively(context, file, dstDir, overwrite)
            } else {
                val foundFile = targetFile.findFile(file.name)
                val fileExists = if (foundFile != null && foundFile.isFile) {
                    if (overwrite) {
                        !foundFile.delete()
                    } else {
                        true
                    }
                } else {
                    false
                }
                if (fileExists) {
                    throw IOException("File already exists.")
                }
                val extension = MimeTypeMap.getFileExtensionFromUrl(file.name)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: throw RuntimeException("Could not retrieve file mime type.")
                val dstFile = targetFile.createFile(mimeType, file.name)
                    ?: throw IOException("Could not create file.")
                copyRecursively(context, file, dstFile, overwrite)
            }
        }
    } else {
        val inputStream = FileInputStream(srcFile)
        val outputStream = context.contentResolver.openOutputStream(targetFile.uri)
            ?: throw IOException("Could not open output stream.")
        val bytesCopied = inputStream.use { input ->
            outputStream.use { output -> input.copyTo(output) }
        }
        if (bytesCopied != srcFile.length()) {
            throw IOException("Source file wasn't copied completely.")
        }
    }
}

private fun copyRecursively(srcFile: DocumentFile, targetFile: File) {

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
            ?: throw NullPointerException("Could not open the output stream")
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