package com.aureusapps.android.extensions

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import okhttp3.internal.closeQuietly
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
 * Recursively deletes the contents of this `DocumentFile`, including all its child files and
 * directories, if any. This function will attempt to delete all the contents of the `DocumentFile`
 * and the `DocumentFile` itself.
 *
 * @return `true` if the deletion was successful, `false` otherwise.
 */
fun DocumentFile.deleteRecursively(): Boolean {
    return walkBottomUp { file ->
        file.delete()
    }
}

/**
 * Recursively copies the contents of this `DocumentFile` to the specified target `DocumentFile`.
 *
 * @param context The `Context` object used for accessing content resolver and other resources.
 * @param targetFile The target `DocumentFile` to which the contents will be copied.
 * @param overwrite Determines whether to overwrite existing files at the target location.
 *                  If set to `true`, existing files will be overwritten; if set to `false`,
 *                  the function will fail if a file with the same name already exists.
 *                  Defaults to `false`.
 * @param onError Called when error occurred. Return true to terminate and false to skip.
 *
 * @return `true` if the copy operation was successful, `false` otherwise.
 */
fun DocumentFile.copyRecursively(
    context: Context,
    targetFile: DocumentFile,
    overwrite: Boolean = false,
    onError: (message: String) -> Boolean = { true }
): Boolean {
    if (!exists()) {
        onError("Source file does not exists.")
        return false
    }
    val srcIsDirectory = isDirectory
    val targetIsDirectory = targetFile.isDirectory
    val dstFile =
        if (srcIsDirectory && targetIsDirectory || targetFile.isFile && targetFile.length() == 0L) {
            targetFile
        } else {
            if (targetFile.uri.scheme == "file") {
                val file = targetFile.uri.toFile()
                if (file.exists()) {
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        file.delete()
                    }
                }
                if (srcIsDirectory) {
                    file.mkdirs()
                } else {
                    file.createNewFile()
                }
                DocumentFile.fromFile(file)
            } else {
                val targetParent = targetFile.parentFile ?: run {
                    onError("Failed to retrieve target parent.")
                    return false
                }
                val targetName = targetFile.name ?: run {
                    onError("Failed to retrieve target name.")
                    return false
                }
                if (targetFile.exists()) {
                    val stillExists = if (!overwrite) true else if (targetIsDirectory) {
                        !targetFile.deleteRecursively()
                    } else {
                        !targetFile.delete()
                    }
                    if (stillExists) {
                        onError("Failed to delete existing file.")
                        return false
                    }
                }
                if (srcIsDirectory) {
                    targetParent.createDirectory(targetName) ?: run {
                        onError("Could not create destination directory.")
                        return false
                    }
                } else {
                    val mimeType = targetFile.type ?: run {
                        onError("Could not retrieve target mime type.")
                        return false
                    }
                    targetParent.createFile(mimeType, targetName) ?: run {
                        onError("Could not create destination file.")
                        return false
                    }
                }
            }
        }
    return copyRecursively(context, this, dstFile, overwrite, onError)
}

@SuppressLint("Recycle")
private fun copyRecursively(
    context: Context,
    srcFile: DocumentFile,
    targetFile: DocumentFile,
    overwrite: Boolean = false,
    onError: (message: String) -> Boolean = { true }
): Boolean {
    if (srcFile.isDirectory) {
        val files = srcFile.listFiles()
        for (src in files) {
            val srcIsDirectory = src.isDirectory
            val srcName = src.name
                ?: if (onError("Source file name is null.")) {
                    return false
                } else {
                    continue
                }
            val existing = targetFile.findFile(srcName)
            val create = if (existing == null) {
                true
            } else {
                val existingIsDirectory = existing.isDirectory
                if (srcIsDirectory && existingIsDirectory) {
                    false
                } else {
                    val stillExists = if (overwrite) {
                        if (existingIsDirectory) {
                            !existing.deleteRecursively()
                        } else {
                            !existing.delete()
                        }
                    } else {
                        true
                    }
                    if (stillExists) {
                        if (onError("Could not delete existing file.")) {
                            return false
                        } else {
                            continue
                        }
                    }
                    true
                }
            }
            val dst = if (create) {
                if (srcIsDirectory) {
                    targetFile.createDirectory(srcName)
                        ?: if (onError("Could not create destination directory.")) {
                            return false
                        } else {
                            continue
                        }
                } else {
                    val mimeType = src.type
                        ?: if (onError("Could not retrieve source file mime type.")) {
                            return false
                        } else {
                            continue
                        }
                    val fileName = srcName.substringBeforeLast(".")
                    targetFile.createFile(mimeType, fileName)
                        ?: if (onError("Could not create destination file.")) {
                            return false
                        } else {
                            continue
                        }
                }
            } else {
                existing!!
            }
            if (!copyRecursively(context, src, dst, overwrite, onError)) {
                return false
            }
        }
        return true
    } else {
        var input: InputStream? = null
        var output: OutputStream? = null
        return try {
            input = context.contentResolver.openInputStream(srcFile.uri)
            output = context.contentResolver.openOutputStream(targetFile.uri)
            if (input != null && output != null) {
                input.copyTo(output)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        } finally {
            input?.closeQuietly()
            output?.closeQuietly()
        }
    }
}