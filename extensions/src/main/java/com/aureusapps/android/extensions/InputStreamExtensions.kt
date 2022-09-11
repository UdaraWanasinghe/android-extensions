package com.aureusapps.android.extensions

import android.content.Context
import android.net.Uri
import java.io.*

fun InputStream.writeTo(path: String, bufferSize: Int = 8192): Boolean {
    return try {
        val file = File(path)
        val output = BufferedOutputStream(FileOutputStream(file))
        val input = BufferedInputStream(this)
        val buffer = ByteArray(bufferSize)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        input.close()
        true
    } catch (e: Exception) {
        false
    }
}

fun InputStream.writeTo(uri: Uri, context: Context, bufferSize: Int = 8192): Boolean {
    return try {
        val output = BufferedOutputStream(context.contentResolver.openOutputStream(uri))
        val input = BufferedInputStream(this)
        val buffer = ByteArray(bufferSize)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        input.close()
        true
    } catch (e: Exception) {
        false
    }
}

fun InputStream.writeTo(out: OutputStream, bufferSize: Int = 8192): Boolean {
    return try {
        val input = BufferedInputStream(this)
        val output = BufferedOutputStream(out)
        val buffer = ByteArray(bufferSize)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        input.close()
        true
    } catch (e: Exception) {
        false
    }
}