package com.aureusapps.android.extensions

import java.io.File

/**
 * Moves this file to the specified destination file.
 *
 * This method will first attempt to rename file to the target file. If the rename fails,
 * it will copy file and delete the original file.
 *
 * @param targetFile The destination file to move to.
 * @param overwrite Whether to overwrite the destination file or it's children if it already exists. Defaults to `false`.
 * @param onError A callback function to be invoked if an exception occurs during the move operation.
 *
 * @return `true` if the move operation was successful, `false` otherwise.
 * @throws Exception if an exception occurs and not recoverable (not handled by the callback function).
 */
fun File.moveTo(
    targetFile: File,
    overwrite: Boolean = false,
    onError: (Exception) -> OnErrorAction = { OnErrorAction.TERMINATE },
): Boolean {
    if (!exists()) {
        return onError(NoSuchFileException(this)) != OnErrorAction.TERMINATE
    }

    if (targetFile.exists()) {
        if (isDirectory) {
            // if target is not a directory, delete it and create directory
            if (!targetFile.isDirectory) {
                val stillExists = !(overwrite && targetFile.delete())
                if (stillExists) {
                    return onError(FileAlreadyExistsException(targetFile)) != OnErrorAction.TERMINATE
                }
                if (!targetFile.mkdirs()) {
                    return onError(FileSystemException(targetFile, reason = "Failed to create directory")) != OnErrorAction.TERMINATE
                }
            }

            // move children to the target directory
            val childFiles = listFiles()!!
            var success = true
            for (childFile in childFiles) {
                val targetChildFile = File(targetFile, childFile.name)
                success = childFile.moveTo(targetChildFile, overwrite, onError)
                if (!success) {
                    break
                }
            }
            return success
        } else {
            val stillExists = !(overwrite && targetFile.delete())
            if (stillExists) {
                return onError(FileAlreadyExistsException(targetFile)) != OnErrorAction.TERMINATE
            }
            return moveTo(targetFile, true, onError)
        }
    }

    // try renaming
    if (renameTo(targetFile)) {
        return true
    }

    // copy and delete
    return copyRecursively(targetFile, overwrite) { _, exception -> onError(exception) } && deleteRecursively()
}