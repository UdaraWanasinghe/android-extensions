package com.aureusapps.android.extensions

import androidx.documentfile.provider.DocumentFile

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
 * Deletes the current [DocumentFile] instance and all its contents recursively,
 * including subdirectories and files.
 *
 * @return `true` if the deletion is successful, `false` otherwise.
 */
fun DocumentFile.deleteRecursively(): Boolean {
    return walkBottomUp { file ->
        file.delete()
    }
}