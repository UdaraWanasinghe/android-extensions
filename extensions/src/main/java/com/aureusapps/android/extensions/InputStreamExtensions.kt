package com.aureusapps.android.extensions

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.*

suspend fun InputStream.writeTo(path: String, bufferSize: Int = 8192): Long {
    return withContext(Dispatchers.IO) {
        val file = File(path)
        val output = BufferedOutputStream(FileOutputStream(file))
        val input = BufferedInputStream(this@writeTo)
        val buffer = ByteArray(bufferSize)
        var read = 0
        var size = 0L
        while (isActive && input.read(buffer).also { read = it } != -1) {
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
        var read = 0
        var size = 0L
        while (isActive && input.read(buffer).also { read = it } != -1) {
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
        var read = 0
        var size = 0L
        while (isActive && input.read(buffer).also { read = it } != -1) {
            size += read
            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        input.close()
        size
    }
}