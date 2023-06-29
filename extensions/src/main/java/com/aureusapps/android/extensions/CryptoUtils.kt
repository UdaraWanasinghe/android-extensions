package com.aureusapps.android.extensions

import android.content.Context
import android.net.Uri
import okhttp3.internal.closeQuietly
import java.io.InputStream
import java.security.MessageDigest

object CryptoUtils {

    /**
     * Generates a hash value for the content represented by the Uri using the specified algorithm.
     *
     * @param context The context used for accessing content resolver.
     * @param uri The Uri representing the content for which to generate the hash.
     * @param algorithm The hashing algorithm to use (e.g., "MD5", "SHA-1", "SHA-256", etc.).
     * @return The hash value as a hexadecimal string, or null if the hash generation fails.
     */
    fun generateHash(context: Context, uri: Uri, algorithm: String): String? {
        var hash: String? = null
        var inputStream: InputStream? = null
        try {
            inputStream = uri.openInputStream(context)
            val digest = MessageDigest.getInstance(algorithm)
            inputStream?.readBytes { buffer, bytesRead ->
                digest.update(buffer, 0, bytesRead)
                true
            }
            val hashBytes = digest.digest()
            val builder = StringBuilder()
            for (byte in hashBytes) {
                builder.append(String.format("%02x", byte))
            }
            hash = builder.toString()

        } catch (_: Exception) {

        } finally {
            inputStream?.closeQuietly()
        }
        return hash
    }

    /**
     * Generates an MD5 hash value for the content represented by the Uri.
     *
     * @param context The context used for accessing content resolver.
     * @param uri The Uri representing the content for which to generate the MD5 hash.
     * @return The MD5 hash value as a hexadecimal string, or null if the hash generation fails.
     */
    fun generateMD5(context: Context, uri: Uri): String? = generateHash(context, uri, "MD5")

    /**
     * Generates a SHA-1 hash value for the content represented by the Uri.
     *
     * @param context The context used for accessing content resolver.
     * @param uri The Uri representing the content for which to generate the SHA-1 hash.
     * @return The SHA-1 hash value as a hexadecimal string, or null if the hash generation fails.
     */
    fun generateSHA1(context: Context, uri: Uri): String? = generateHash(context, uri, "SHA-1")

}