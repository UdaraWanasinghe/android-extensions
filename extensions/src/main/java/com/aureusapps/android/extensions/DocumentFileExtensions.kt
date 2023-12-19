package com.aureusapps.android.extensions

import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.io.InputStream
import java.io.OutputStream

/**
 * Performs a bottom-up traversal of the file hierarchy rooted at the current [DocumentFile] instance,
 * invoking the specified [action] on each file.
 *
 * @param action the action to be performed on each visited file. The file itself is passed as a parameter to the action. Return true from the action if want to continue the traversal.
 *
 * @return true if traversal completed successfully, otherwise false.
 */
fun DocumentFile.walkBottomUp(action: (DocumentFile) -> Boolean): Boolean {
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
 * Performs a top-down traversal of the file hierarchy rooted at the current [DocumentFile] instance,
 * invoking the specified [action] on each file.
 *
 * @param action the action to be performed on each visited file. The file itself is passed as a parameter to the action. Return true from the action if want to continue the traversal.
 *
 * @return true if traversal completed successfully, otherwise false.
 */
fun DocumentFile.walkTopDown(action: (DocumentFile) -> Boolean): Boolean {
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
 * Opens an input stream to read from the DocumentFile using the provided context.
 *
 * @param context The context used to access the content resolver.
 * @return An InputStream to read from the DocumentFile, or null if an error occurs.
 */
fun DocumentFile.openInputStream(context: Context): InputStream? {
    return context.contentResolver.openInputStream(uri)
}

/**
 * Opens an output stream to write to the DocumentFile using the provided context.
 *
 * @param context The context used to access the content resolver.
 * @return An OutputStream to write to the DocumentFile, or null if an error occurs.
 */
fun DocumentFile.openOutputStream(context: Context): OutputStream? {
    return context.contentResolver.openOutputStream(uri)
}

/**
 * Copies the current DocumentFile to a target parent DocumentFile.
 *
 * @param context The android context.
 * @param targetParent The parent DocumentFile to which the current DocumentFile should be copied.
 * @param overwrite Determines whether to overwrite the target file if it already exists. Default is false.
 * @param onError A lambda function that handles errors during the copy operation.
 *                It takes an error message as input and returns a boolean indicating whether
 *                the operation should be terminated (true) or continued (false). Default is to terminate.
 * @return true if the copy operation is successful, false otherwise.
 */
fun DocumentFile.copyTo(
    context: Context,
    targetParent: DocumentFile,
    overwrite: Boolean = false,
    onError: (String) -> Boolean = { true }
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