package com.aureusapps.android.extensions

import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.aureusapps.android.providerfile.ProviderFile
import java.io.File
import java.io.InputStream
import java.io.OutputStream

data class ProviderFileException(
    val file: ProviderFile,
    override val message: String = "Provider file exception",
) : Exception(message)

/**
 * Performs a bottom-up traversal of the file hierarchy rooted at the current [ProviderFile] instance,
 * invoking the specified [action] on each file.
 *
 * @param action the action to be performed on each visited file. The file itself is passed as a parameter to the action. Return true from the action if want to continue the traversal.
 *
 * @return true if traversal completed successfully, otherwise false.
 */
fun ProviderFile.walkBottomUp(action: (ProviderFile) -> Boolean): Boolean {
    if (isDirectory) {
        val files = listFiles()
        for (file in files) {
            if (!file.walkBottomUp(action)) {
                return false
            }
        }
    }
    return action(this)
}

/**
 * Performs a top-down traversal of the file hierarchy rooted at the current [ProviderFile] instance,
 * invoking the specified [action] on each file.
 *
 * @param action the action to be performed on each visited file. The file itself is passed as a parameter to the action. Return true from the action if want to continue the traversal.
 *
 * @return true if traversal completed successfully, otherwise false.
 */
fun ProviderFile.walkTopDown(action: (ProviderFile) -> Boolean): Boolean {
    val files = mutableListOf(this)
    while (files.isNotEmpty()) {
        val file = files.removeAt(0)
        if (!action(file)) return false
        if (file.isDirectory) {
            files.addAll(file.listFiles())
        }
    }
    return true
}

/**
 * Opens an input stream to read from the ProviderFile using the provided context.
 *
 * @param context The context used to access the content resolver.
 * @return An InputStream to read from the ProviderFile, or null if an error occurs.
 */
fun ProviderFile.openInputStream(context: Context): InputStream? {
    return uri.openInputStream(context)
}

/**
 * Opens an output stream to write to the ProviderFile using the provided context.
 *
 * @param context The context used to access the content resolver.
 * @return An OutputStream to write to the ProviderFile, or null if an error occurs.
 */
fun ProviderFile.openOutputStream(context: Context, mode: String = "w"): OutputStream? {
    return context.contentResolver.openOutputStream(uri, mode)
}

/**
 * Copies the current ProviderFile to a target parent ProviderFile.
 *
 * @param context The android context.
 * @param targetParent The parent ProviderFile to which the current ProviderFile should be copied.
 * @param overwrite Determines whether to overwrite the target file if it already exists. Default is false.
 * @param onError A lambda function that handles errors during the copy operation.
 *                It takes an error message as input and returns a boolean indicating whether
 *                the operation should be continued (true) or terminated (false). Default is to terminate.
 * @return true if the copy operation is successful without skipping anything, false otherwise.
 */
fun ProviderFile.copyTo(
    context: Context,
    targetParent: ProviderFile,
    overwrite: Boolean = false,
    onError: (String) -> Boolean = { false },
): Boolean {
    if (!this.exists()) {
        onError("The source file doesn't exist.")
        return false
    }
    val srcIsDirectory = this.isDirectory
    val srcName = this.name ?: run {
        onError("Couldn't retrieve document name.")
        return false
    }
    val existing = targetParent.findFile(srcName)
    val dst = if (existing != null && existing.isDirectory) {
        existing
    } else {
        if (existing != null) {
            if (overwrite) {
                if (!existing.delete()) {
                    onError("Tried to overwrite the destination, but failed to delete it.")
                    return false
                }
            } else {
                onError("The destination file already exists.")
                return false
            }
        }
        if (srcIsDirectory) {
            targetParent.createDirectory(srcName) ?: run {
                onError("Failed to create directory.")
                return false
            }
        } else {
            val mimeType = if (targetParent.uri.scheme != SCHEME_FILE) {
                val extension = srcName.substringAfterLast(".")
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""
            } else {
                ""
            }
            targetParent.createFile(mimeType, srcName) ?: run {
                onError("Failed to create file.")
                return false
            }
        }
    }
    if (srcIsDirectory) {
        val files = listFiles()
        var result = true
        for (src in files) {
            var terminate = false
            val copied = src.copyTo(context, dst, overwrite) { message ->
                onError(message).also { terminate = it }
            }
            if (!copied) {
                if (result) result = false
                if (terminate) return false
            }
        }
        return result
    } else {
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            input = openInputStream(context)
            output = dst.openOutputStream(context)
            if (input != null && output != null) {
                return input.copyTo(output) == length()
            }
        } finally {
            input?.close()
            output?.close()
        }
        return false
    }
}

/**
 * Writes given provider file to the given target file.
 *
 * @param context The android context.
 * @param targetFile The target file to which the provider file should be written.
 * @param writeMode The write mode for the target file.
 *
 * @return The number of bytes written to the target file.
 */
fun ProviderFile.writeTo(
    context: Context,
    targetFile: ProviderFile,
    writeMode: String = "w",
): Long {
    var input: InputStream? = null
    var output: OutputStream? = null
    try {
        input = openInputStream(context)
            ?: throw ProviderFileException(this, "Failed to open input stream")
        output = targetFile.openOutputStream(context, writeMode)
            ?: throw ProviderFileException(targetFile, "Failed to open output stream")
        return input.writeTo(output)
    } finally {
        output?.flush()
        output?.closeQuietly()
        input?.closeQuietly()
    }
}

/**
 * Moves the current provider file to a target specified by [targetParent].
 *
 * @param context The android context.
 * @param targetParent The target parent provider file to which the current provider file should be moved.
 * @param overwrite Whether to overwrite the target provider file if it already exists.
 * @param onError A lambda function that handles errors during the move operation.
 *
 * @return true if all files moved or moved with some skipped, or false if failed or terminated on error.
 */
fun ProviderFile.moveToParent(
    context: Context,
    targetParent: ProviderFile,
    overwrite: Boolean,
    onError: (Exception) -> OnErrorAction,
): Boolean {
    if (!exists()) {
        return onError(ProviderFileException(this, "Source file does not exists")) != OnErrorAction.TERMINATE
    }

    // if both provider files are RawProviderFiles, use file move operation
    val srcUri = uri
    if (srcUri.isFileUri) {
        val srcFile = srcUri.toFile()
        val targetParentUri = targetParent.uri
        if (targetParentUri.isFileUri) {
            val targetParentFile = targetParentUri.toFile()
            val targetFile = File(targetParentFile, srcFile.name)
            return srcFile.moveTo(targetFile, overwrite, onError)
        }
    }

    val fileName = name ?: return onError(ProviderFileException(this, "Couldn't retrieve document name")) != OnErrorAction.TERMINATE
    val isDirectory = isDirectory
    var targetFile = targetParent.findFile(fileName)
    if (targetFile == null) {
        targetFile = if (isDirectory) {
            createProviderDirectory(targetParent, fileName)
        } else {
            createProviderFile(targetParent, fileName)
        }
    } else {
        if (isDirectory) {
            if (!targetFile.isDirectory) {
                deleteProviderFile(targetFile, overwrite)
                createProviderDirectory(targetParent, fileName)
            }
        }
    }

    if (isDirectory) {
        for (childFile in listFiles()) {
            if (!childFile.moveToParent(context, targetFile, overwrite, onError)) {
                return false
            }
        }
        return true
    } else {
        return writeTo(context, targetFile, "wt") == length()
    }
}

/**
 * Moves the current providerFile to a target specified by [targetFile].
 *
 * @param context The android context.
 * @param targetFile The target provider file to which current provider file should be copied.
 * @param overwrite Whether to overwrite the target provider file if exists.
 * @param onError A lambda function that handles errors during the move operation.
 *                It takes an error message as input and returns a boolean indicating whether
 *                the operation should be continued (true) or terminated (false). Default is terminate.
 *
 * @return true if the move operation was successful without skipping anything, false otherwise.
 */
fun ProviderFile.moveTo(
    context: Context,
    targetFile: ProviderFile,
    overwrite: Boolean = false,
    onError: (Exception) -> OnErrorAction = { OnErrorAction.TERMINATE },
): Boolean {
    if (!exists()) {
        return onError(ProviderFileException(this, "Source provider file does not exists")) != OnErrorAction.TERMINATE
    }

    val srcUri = uri
    if (srcUri.isFileUri) {
        val srcFile = srcUri.toFile()
        val dstUri = targetFile.uri
        if (dstUri.isFileUri) {
            val dstFile = dstUri.toFile()
            return srcFile.moveTo(dstFile, overwrite, onError)
        }
    }

    val isDirectory = isDirectory
    val targetIsDirectory = targetFile.isDirectory

    val targetProviderFile: ProviderFile
    if (isDirectory == targetIsDirectory) {
        targetProviderFile = targetFile
    } else {
        val targetFileParent = targetFile.parent
            ?: return onError(ProviderFileException(targetFile, "Destination provider file does not have a parent")) != OnErrorAction.TERMINATE
        deleteProviderFile(targetFile, overwrite)
        val fileName = name ?: return onError(ProviderFileException(this, "Couldn't retrieve document name")) != OnErrorAction.TERMINATE
        targetProviderFile = if (isDirectory) {
            createProviderDirectory(targetFileParent, fileName)
        } else {
            createProviderFile(targetFileParent, fileName)
        }
    }

    return if (isDirectory) {
        for (childFile in listFiles()) {
            if (!childFile.moveToParent(context, targetProviderFile, overwrite, onError)) {
                return false
            }
        }
        delete()
    } else {
        writeTo(context, targetProviderFile, "wt") == length() && delete()
    }
}

/**
 * Creates a provider directory in the given parent directory.
 * Make sure the parent directory exists before calling this function.
 * Make sure there is no directory with the same name in the parent directory.
 */
private fun createProviderDirectory(parentDir: ProviderFile, dirName: String): ProviderFile {
    return parentDir.createDirectory(dirName)
        ?: throw ProviderFileException(parentDir, "Failed to create directory in the provider file")
}

/**
 * Creates a provider file in the given parent directory.
 * Make sure the parent directory exists before calling this function.
 * Make sure there is no file with the same name in the parent directory.
 */
private fun createProviderFile(parentDir: ProviderFile, fileName: String): ProviderFile {
    val displayName = fileName.substringBeforeLast(".")
    val extension = fileName.substringAfterLast(".")
    val mimeType = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(extension)
        ?: "application/octet-stream"
    return parentDir.createFile(mimeType, displayName)
        ?: throw ProviderFileException(parentDir, "Failed to create file in the provider file")
}

/**
 * Deletes the given provider file.
 * Call this if you sure provider [file] exists.
 */
private fun deleteProviderFile(file: ProviderFile, overwrite: Boolean) {
    val stillExists = !(overwrite && file.delete())
    if (stillExists) {
        throw ProviderFileException(file, "Destination file already exists")
    }
}
