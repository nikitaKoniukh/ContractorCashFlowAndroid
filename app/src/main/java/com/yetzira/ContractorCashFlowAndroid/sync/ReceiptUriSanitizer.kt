package com.yetzira.ContractorCashFlowAndroid.sync

/**
 * Receipt images live only on the capturing device (local file/content URIs).
 * Syncing those paths to Firestore breaks other devices; strip them on write/read.
 */
object ReceiptUriSanitizer {
    fun forCloudSync(uri: String?): String? {
        if (uri.isNullOrBlank()) return null
        return if (isLocalDeviceUri(uri)) null else uri
    }

    fun forLocalMerge(uri: String?): String? {
        if (uri.isNullOrBlank()) return null
        return if (isLocalDeviceUri(uri)) null else uri
    }

    fun isLocalDeviceUri(uri: String): Boolean {
        val value = uri.trim()
        return value.startsWith("file:", ignoreCase = true) ||
            value.startsWith("content:", ignoreCase = true) ||
            value.startsWith("/") // absolute filesystem path
    }
}
