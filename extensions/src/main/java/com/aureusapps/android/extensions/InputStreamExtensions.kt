package com.aureusapps.android.extensions

import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Copies all bytes from this input stream to the specified output stream [out].
 *
 * @param out The output stream to write bytes to.
 * @param bufferSize The size of the buffer to use for copying (default is 8192 bytes).
 *
 * @return The total number of bytes copied.
 */
fun InputStream.writeTo(
    out: OutputStream,
    bufferSize: Int = 8192,
): Long {
    var output: OutputStream? = null
    return try {
        val input = BufferedInputStream(this@writeTo)
        output = BufferedOutputStream(out)
        val buffer = ByteArray(bufferSize)
        var read: Int
        var size = 0L
        while (input.read(buffer).also { read = it } != -1) {
            size += read
            output.write(buffer, 0, read)
        }
        size
    } finally {
        output?.flush()
    }
}

/**
 * Copies all bytes from this input stream to the file specified by [path].
 *
 * @param path The path of the file to write bytes to.
 * @param bufferSize The size of the buffer to use for copying (default is 8192 bytes).
 *
 * @return The total number of bytes copied.
 */
fun InputStream.writeTo(
    path: String,
    bufferSize: Int = 8192,
): Long {
    var output: OutputStream? = null
    return try {
        val file = File(path)
        output = FileOutputStream(file)
        writeTo(output, bufferSize)
    } finally {
        output?.closeQuietly()
    }
}

/**
 * Writes all bytes from this input stream to the content specified by [uri].
 *
 * @param uri The URI where the content will be written to.
 * @param context The context used to resolve the URI.
 * @param bufferSize The size of the buffer to use for copying (default is 8192 bytes).
 *
 * @return The total number of bytes copied.
 */
fun InputStream.writeTo(
    uri: Uri,
    context: Context,
    bufferSize: Int = 8192,
): Long {
    var output: OutputStream? = null
    return try {
        output = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("Failed to open output stream")
        writeTo(output, bufferSize)
    } finally {
        output?.closeQuietly()
    }
}

/**
 * Reads data from the input stream in chunks and passes the buffer and the number of bytes read
 * to the provided consumer function until the end of the stream is reached or the consumer function
 * returns false.
 *
 * @param bufferSize The size of the buffer used for reading data. Default value is 8192 bytes.
 * @param consumer The function that consumes the buffer and the number of bytes read. It should return
 *                 true to continue reading or false to stop reading.
 * @return The total number of bytes read from the input stream.
 */
fun InputStream.readBytes(
    bufferSize: Int = 8192,
    consumer: (buffer: ByteArray, bytesRead: Int) -> Boolean,
): Int {
    val buffer = ByteArray(bufferSize)
    var bytesRead: Int
    var totalBytes = 0
    while (read(buffer).also { bytesRead = it } != -1 && consumer(buffer, bytesRead)) {
        totalBytes += bytesRead
    }
    return totalBytes
}

/**
 * Reads the content of this input stream into a byte array.
 *
 * @param bufferSize The size of the buffer used for reading. Default is 8192 bytes.
 * @return A byte array containing the content of the input stream.
 */
fun InputStream.readBytes(bufferSize: Int = 8192): ByteArray {
    val output = ByteArrayOutputStream()
    copyTo(output, bufferSize)
    return output.toByteArray()
}

/**
 * Reads the content of this input stream and converts it to a string.
 *
 * @return The content of the input stream as a string.
 */
fun InputStream.readText(bufferSize: Int = 8192): String {
    return String(readBytes(bufferSize))
}