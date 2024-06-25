package com.aureusapps.android.extensions

import java.io.File

data class FileException(
    val file: File,
    override val message: String = "File exception",
) : Exception(message)

/**
 * Moves this file to the specified destination file.
 *
 * @param targetFile The destination file to move to.
 * @param overwrite Whether to overwrite the destination file or it's children if it already exists. Defaults to `false`.
 * @param onError A callback function to be invoked if an exception occurs during the move operation. Return true to continue operation or false to terminate it.
 *
 * @return `true` if the move operation was successful, `false` otherwise.
 */
fun File.moveTo(
    targetFile: File,
    overwrite: Boolean = false,
    onError: (Exception) -> OnErrorAction = { OnErrorAction.TERMINATE },
): Boolean {
    // check source file
    if (!exists()) {
        return onError(FileException(this, "Source file does not exists")) != OnErrorAction.TERMINATE
    }

    if (targetFile.exists()) {
        if (isDirectory) {
            if (!targetFile.isDirectory) {
                val stillExists = !(overwrite && targetFile.delete())
                if (stillExists) {
                    return onError(FileException(targetFile, "Target file exists")) != OnErrorAction.TERMINATE
                }
                if (!targetFile.mkdirs()) {
                    return onError(FileException(targetFile, "Failed to create target file")) != OnErrorAction.TERMINATE
                }
            }

            val childFiles = listFiles() ?: return true
            var success = true
            for (childFile in childFiles) {
                var skip = false
                val targetChildFile = File(targetFile, childFile.name)
                childFile.moveTo(targetChildFile, overwrite) { exception ->
                    onError(exception).also {
                        skip = it == OnErrorAction.SKIP
                        if (success) {
                            success = it != OnErrorAction.TERMINATE
                        }
                    }
                }
                if (skip) {
                    break
                }
            }
            return success
        } else {
            val stillExists = !(overwrite && targetFile.delete())
            if (stillExists) {
                return onError(FileException(targetFile, "Target file exists")) != OnErrorAction.TERMINATE
            }
            return moveTo(targetFile, true, onError)
        }
    }

    // try renaming
    if (renameTo(targetFile)) {
        return true
    }

    // copy and delete
    return copyRecursively(targetFile, overwrite) { _, exception ->
        onError(exception)
    } && deleteRecursively()
}