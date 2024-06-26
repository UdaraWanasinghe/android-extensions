package com.aureusapps.android.extensions

import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.aureusapps.android.providerfile.ProviderFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException
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
        val file = files.removeFirst()
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
    return context.contentResolver.openInputStream(uri)
}

/**
 * Opens an output stream to write to the ProviderFile using the provided context.
 *
 * @param context The context used to access the content resolver.
 * @return An OutputStream to write to the ProviderFile, or null if an error occurs.
 */
fun ProviderFile.openOutputStream(context: Context): OutputStream? {
    return context.contentResolver.openOutputStream(uri)
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

fun ProviderFile.moveToParent(
    context: Context,
    targetParent: ProviderFile,
    overwrite: Boolean,
    onError: (Exception) -> OnErrorAction,
): Boolean {
    if (!exists()) {
        return onError(ProviderFileException(this, "Source file does not exists")) != OnErrorAction.TERMINATE
    }

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
        if (isDirectory) {
            targetFile = targetParent.createDirectory(fileName)
            if (targetFile == null) {
                return onError(ProviderFileException(targetParent, "Failed to create directory in the provider file")) != OnErrorAction.TERMINATE
            }
        } else {
            val mimeType = fileName.substringAfterLast(".")
            val displayName = fileName.substringBeforeLast(".")
            targetFile = targetParent.createFile(mimeType, displayName)
            if (targetFile == null) {
                return onError(ProviderFileException(targetParent, "Failed to create file in the provider file")) != OnErrorAction.TERMINATE
            }
        }
    }
    return moveTo(context, targetFile, overwrite, onError)
}

/**
 * Moves the current providerFile to a target specified by [dstProviderFile].
 *
 * @param context The android context.
 * @param dstProviderFile The target provider file to which current provider file should be copied.
 * @param overwrite Whether to overwrite the target provider file if exists.
 * @param onError A lambda function that handles errors during the move operation.
 *                It takes an error message as input and returns a boolean indicating whether
 *                the operation should be continued (true) or terminated (false). Default is terminate.
 * @return true if the move operation was successful without skipping anything, false otherwise.
 */
fun ProviderFile.moveTo(
    context: Context,
    dstProviderFile: ProviderFile,
    overwrite: Boolean = false,
    onError: (Exception) -> OnErrorAction,
): Boolean {
    if (!exists()) {
        onError(ProviderFileException(this, "Source provider file does not exists"))
        return false
    }

    val srcUri = uri
    if (srcUri.isFileUri) {
        val srcFile = srcUri.toFile()
        var dstUri = dstProviderFile.uri
        if (dstUri.isFileUri) {
            val dstFile = dstUri.toFile()
            return srcFile.moveTo(dstFile, overwrite, onError)
        } else {
            // dst provider file is not a file
            if (srcFile.isDirectory) {
                if (dstProviderFile.isDirectory) {
                    // now move children
                    val childFiles = srcFile.listFiles()
                    if (childFiles != null) {
                        var success = true
                        for (childFile in childFiles) {
                            var resume = true
                            srcFile.moveToParent(context, dstProviderFile, overwrite) { exception ->
                                success = false
                                onError(exception).also { resume = it }
                            }
                            if (!resume) {
                                break
                            }
                        }
                        return success
                    }
                } else {
                    val parentProviderFile = dstProviderFile.parent
                    if (parentProviderFile != null) {
                        return srcFile.moveToParent(context, parentProviderFile, overwrite, onError)
                    }
                    onError(ProviderFileException(dstProviderFile, "Destination provider file exists"))
                    return false
                }
            } else {
                if (dstProviderFile.isDirectory) {
                    // if destination provider file is a directory
                    // try to find parent directory and delete the destination provider file
                    // after deleting, create a new file inside the parent directory
                    val parentProviderFile = dstProviderFile.parent
                    val stillExists = parentProviderFile == null || !(overwrite && dstProviderFile.delete())
                    if (stillExists) {
                        onError(ProviderFileException(dstProviderFile, "Destination provider file already exists"))
                        return false
                    }
                    // create file inside parent directory
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(srcFile.extension)
                        ?: "application/octet-stream"
                    val newDstProviderFile = parentProviderFile?.createFile(mimeType, srcFile.name)
                    if (newDstProviderFile == null) {
                        onError(ProviderFileException(parentProviderFile!!, "Failed to create file in the provider file"))
                        return false
                    }
                    dstUri = newDstProviderFile.uri
                }
                var input: InputStream? = null
                var output: OutputStream? = null
                try {
                    input = FileInputStream(srcFile)
                    output = context.contentResolver.openOutputStream(dstUri, "wt")
                        ?: throw IOException("Failed to open output stream")
                    var success = true
                    input.writeTo(output) { exception ->
                        success = false
                        onError(exception)
                    }
                    return success
                } catch (e: Exception) {
                    onError(e)
                    return false
                } finally {
                    input?.closeQuietly()
                    output?.closeQuietly()
                }
            }
        }
    }
}