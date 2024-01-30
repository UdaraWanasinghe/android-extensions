package com.aureusapps.android.extensions

import java.security.SecureRandom

object RandomUtils {

    private const val KEY_LENGTH_BYTES = 16

    /**
     * Generates a unique key string.
     *
     * This function generates a random unique key using a cryptographically secure random number generator.
     * The generated key is represented as a hexadecimal string.
     *
     * @return A unique key string.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun generateUniqueKey(): String {
        val randomBytes = ByteArray(KEY_LENGTH_BYTES)
        SecureRandom().nextBytes(randomBytes)
        return randomBytes.toHexString()
    }

}