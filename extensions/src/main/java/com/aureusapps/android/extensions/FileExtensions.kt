package com.aureusapps.android.extensions

import android.content.Context
import android.webkit.MimeTypeMap
import com.aureusapps.android.providerfile.ProviderFile
import java.io.File
import java.io.FileInputStream

data class FileException(
    val file: File,
    override val message: String = "File exception",
) : Exception(message)

data class CopyFileFailedException(
    val srcFile: File,
    val dstFile: File,
    override val message: String = "Failed to copy from source file to destination file recursively",
) : Exception(message)

data class CreateProviderFileFailedException(
    val parentFile: ProviderFile,
    val fileName: String,
    override val message: String = "Failed to create provider file",
) : Exception(message)

data class ProviderFileException(
    val file: ProviderFile,
    override val message: String = "Provider file exception",
) : Exception(message)

/**
 * Moves this file to the specified destination file.
 *
 * @param dstFile The destination file to move to.
 * @param overwrite Whether to overwrite the destination file or it's children if it already exists. Defaults to `false`.
 * @param onError A callback function to be invoked if an exception occurs during the move operation. Return true to continue operation or false to terminate it.
 *
 * @return `true` if the move operation was successful, `false` otherwise.
 */
fun File.moveTo(
    dstFile: File,
    overwrite: Boolean = false,
    onError: (Exception) -> Boolean = { false },
): Boolean {
    // check source file
    if (!exists()) {
        onError(FileException(this, "Source file not found"))
        return false
    }

    // check destination file
    val stillExists = dstFile.exists() && !(overwrite && dstFile.deleteRecursively())
    if (stillExists) {
        onError(FileException(dstFile, "Destination file exists"))
        return false
    }

    // try renaming
    if (renameTo(dstFile)) {
        return true
    }

    // try copy and delete
    val copied = copyRecursively(dstFile, overwrite) { _, exception ->
        if (onError(exception)) {
            OnErrorAction.SKIP
        } else {
            OnErrorAction.TERMINATE
        }
    }
    return if (copied) {
        val deleted = deleteRecursively()
        onError(FileException(this, "Failed to delete file"))
        return deleted
    } else {
        onError(CopyFileFailedException(this, dstFile))
        false
    }
}

/**
 * Copies this file to the location specified by [parentDir] within the given [context].
 *
 * @param context The context used to access content providers or other resources.
 * @param parentDir The provider file representing the destination directory where this file will be copied.
 * @param overwrite If `true`, the existing file at the destination will be overwritten. Default is `false`.
 * @param onError Optional handler for errors that occur during copying. The lambda returns a boolean indicating whether to continue (`true`) or to abort (`false`) the operation upon encountering an error.
 * @return `true` if the file was successfully copied, `false` otherwise.
 */
fun File.copyTo(
    context: Context,
    parentDir: ProviderFile,
    overwrite: Boolean = false,
    onError: (Exception) -> Boolean = { false },
): Boolean {
    if (!exists()) {
        onError(FileException(this, "Source file not found"))
        return false
    }

    if (isDirectory) {
        var currentFile = parentDir.findFile(name)
        val shouldCreateDir: Boolean = when {
            currentFile == null -> true
            currentFile.isDirectory -> false
            else -> {
                val stillExists = !(overwrite && currentFile.delete())
                if (stillExists) {
                    onError(ProviderFileException(currentFile, "Destination provider file exists"))
                    return false
                }
                true
            }
        }
        if (shouldCreateDir) {
            currentFile = parentDir.createDirectory(name)
            if (currentFile == null) {
                onError(CreateProviderFileFailedException(parentDir, name))
                return false
            }
        }

        // at this point existing is a directory
        // now copy children to the existing
        val childFiles = listFiles()
        var success = true
        if (childFiles != null) {
            for (childFile in childFiles) {
                var resume = true
                childFile.copyTo(context, currentFile!!, overwrite) { exception ->
                    success = false
                    onError(exception).also { resume = it }
                }
                if (!resume) {
                    break
                }
            }
        }
        return success
    } else {
        val currentFile = parentDir.findFile(name)
        val stillExists = currentFile != null && !(overwrite && currentFile.delete())
        if (stillExists) {
            onError(ProviderFileException(currentFile!!, "Destination provider file exists"))
            return false
        }
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
        val newFile = parentDir.createFile(mimeType, nameWithoutExtension)
        if (newFile == null) {
            onError(CreateProviderFileFailedException(parentDir, name))
            return false
        }
        val outputStream = newFile.openOutputStream(context)
        if (outputStream == null) {
            onError(ProviderFileException(newFile, "Failed to open provider file output stream"))
            return false
        }
        val inputStream = FileInputStream(this)
        inputStream.writeTo(outputStream) { exception ->
            throw exception
        }
        return true
    }
}