package com.aureusapps.android.extensions

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.graphics.RectF
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.io.OutputStream

object BitmapUtils {

    /**
     * Decodes the given Uri into a Bitmap image.
     *
     * @param context The context in which the function is called.
     * @param uri The Uri of the image to be decoded.
     * @param options An optional set of decoding options. If null, default decoding options are used.
     * This could be content provider uri, android resource uri, file uri or a http uri.
     *
     * @return The decoded Bitmap image, or null if decoding fails.
     */
    @JvmStatic
    fun decodeUri(context: Context, uri: Uri, options: BitmapFactory.Options? = null): Bitmap? {
        var inputStream: InputStream? = null
        var bitmap: Bitmap? = null

        try {
            when (uri.scheme) {
                ContentResolver.SCHEME_CONTENT, ContentResolver.SCHEME_ANDROID_RESOURCE, ContentResolver.SCHEME_FILE -> {
                    inputStream = context.contentResolver.openInputStream(uri)
                }

                "http", "https" -> {
                    val request = Request.Builder().url(uri.toString()).build()
                    val client = OkHttpClient.Builder().build()
                    val response = client.newCall(request).execute()
                    val body = response.body
                    if (body != null) {
                        if (response.code == 200) {
                            inputStream = body.byteStream()
                        } else {
                            response.close()
                        }
                    }
                }
            }
            bitmap = if (inputStream != null) {
                BitmapFactory.decodeStream(inputStream, null, options)
            } else {
                null
            }

        } catch (_: Exception) {

        } finally {
            inputStream?.close()
        }

        return bitmap
    }

    /**
     * Saves a Bitmap image in the specified directory using the given display name.
     *
     * @param context The Android context object.
     * @param bitmap The Bitmap image to be saved.
     * @param directoryUri The Uri of the directory where the image should be saved.
     * This could be a file uri or a document tree uri.
     * @param fileName The display name of the image file.
     * @param compressFormat The format to use for compressing the image (default: PNG).
     * @param compressQuality The quality level to use for compressing the image (default: 100).
     *
     * @return The Uri of the saved image file, or null if the saving operation fails.
     */
    @JvmStatic
    fun saveInDirectory(
        context: Context,
        bitmap: Bitmap,
        directoryUri: Uri,
        fileName: String,
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        compressQuality: Int = 100,
    ): Uri? {
        var outputStream: OutputStream? = null
        var uri: Uri? = null
        try {
            val newUri = directoryUri.createFile(
                context, fileName
            )
            if (newUri != null) {
                outputStream = context.contentResolver.openOutputStream(newUri)
                if (outputStream != null && bitmap.compress(
                        compressFormat, compressQuality, outputStream
                    )
                ) {
                    uri = newUri
                }
            }

        } finally {
            outputStream?.close()
        }
        return uri
    }

    /**
     * Scales the provided bitmap to the specified width and height using the given scale type.
     *
     * @param bitmap The bitmap to be scaled.
     * @param width The target width for the scaled bitmap.
     * @param height The target height for the scaled bitmap.
     * @param stf The scaling behavior to be applied. Default is Matrix.ScaleToFit.FILL.
     *
     * @return The scaled bitmap.
     */
    fun scaleBitmap(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        stf: Matrix.ScaleToFit = Matrix.ScaleToFit.FILL,
    ): Bitmap {
        val matrix = Matrix()
        val srcRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val dstRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        matrix.setRectToRect(srcRect, dstRect, stf)
        val resizedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resizedBitmap)
        val filterFlag = if (srcRect.width() < dstRect.width() && srcRect.height() < dstRect.height()) {
            FILTER_BITMAP_FLAG
        } else {
            0
        }
        val paint = Paint(ANTI_ALIAS_FLAG or filterFlag)
        canvas.drawBitmap(bitmap, matrix, paint)
        return resizedBitmap
    }

}