package com.aureusapps.android.extensions

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

suspend fun InputStream.writeTo(path: String, bufferSize: Int = 8192): Long {
    return withContext(Dispatchers.IO) {
        val file = File(path)
        val output = BufferedOutputStream(FileOutputStream(file))
        val input = BufferedInputStream(this@writeTo)
        val buffer = ByteArray(bufferSize)
        var read: Int
        var size = 0L
        while (input.read(buffer).also { read = it } != -1) {
            size += read
            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        input.close()
        size
    }
}

suspend fun InputStream.writeTo(uri: Uri, context: Context, bufferSize: Int = 8192): Long {
    return withContext(Dispatchers.IO) {
        val output = BufferedOutputStream(context.contentResolver.openOutputStream(uri))
        val input = BufferedInputStream(this@writeTo)
        val buffer = ByteArray(bufferSize)
        var read: Int
        var size = 0L
        while (input.read(buffer).also { read = it } != -1) {
            size += read
            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        input.close()
        size
    }
}

suspend fun InputStream.writeTo(out: OutputStream, bufferSize: Int = 8192): Long {
    return withContext(Dispatchers.IO) {
        val input = BufferedInputStream(this@writeTo)
        val output = BufferedOutputStream(out)
        val buffer = ByteArray(bufferSize)
        var read: Int
        var size = 0L
        while (input.read(buffer).also { read = it } != -1) {
            size += read
            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        input.close()
        size
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
    consumer: (buffer: ByteArray, bytesRead: Int) -> Boolean
): Int {
    val buffer = ByteArray(bufferSize)
    var bytesRead: Int
    var totalBytes = 0
    while (read(buffer).also { bytesRead = it } != -1 && consumer(buffer, bytesRead)) {
        totalBytes += bytesRead
    }
    return totalBytes
}